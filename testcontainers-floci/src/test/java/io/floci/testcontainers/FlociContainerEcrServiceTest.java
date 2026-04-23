package io.floci.testcontainers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.testcontainers.DockerClientFactory;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.ImageIdentifier;
import software.amazon.awssdk.services.ecr.model.Repository;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(OrderAnnotation.class)
class FlociContainerEcrServiceTest extends AbstractFlociContainerServiceTest {

    private static final String SOURCE_IMAGE_NAME = "alpine";
    private static final String SOURCE_IMAGE_TAG = "3.20";
    private static final String SOURCE_IMAGE = SOURCE_IMAGE_NAME + ":" + SOURCE_IMAGE_TAG;

    static EcrClient ecr;
    static String repoName;
    static String ecrImageName;
    static String ecrImageRef;
    static AuthConfig authConfig;

    @BeforeAll
    static void setUp() {
        ecr = client(EcrClient.builder());
        repoName = "test-repo-" + System.currentTimeMillis();

        String registryUrl = floci.getHost() + ":" + floci.getEcrConfig().getRegistryBasePort();
        ecrImageName = registryUrl + "/" + repoName;
        ecrImageRef = ecrImageName + ":" + SOURCE_IMAGE_TAG;

        // Obtain ECR authorization
        var authData = ecr.getAuthorizationToken().authorizationData().get(0);
        byte[] decoded = Base64.getDecoder().decode(authData.authorizationToken());
        String[] credentials = new String(decoded).split(":", 2);
        authConfig = new AuthConfig().withUsername(credentials[0]).withPassword(credentials[1]);
    }

    @Test
    @Order(1)
    void shouldCreateRepository() {
        Repository repo = ecr.createRepository(b -> b.repositoryName(repoName)).repository();

        assertThat(repo.repositoryArn()).isNotBlank();
        assertThat(repo.repositoryUri()).isNotBlank();
    }

    @Test
    @Order(2)
    void shouldDescribeRepository() {
        List<Repository> repos = ecr.describeRepositories(b -> b.repositoryNames(repoName)).repositories();

        assertThat(repos).hasSize(1);
        assertThat(repos.get(0).repositoryName()).isEqualTo(repoName);
    }

    @Test
    @Order(3)
    void shouldListNoImagesWhenEmpty() {
        var imageIds = ecr.listImages(b -> b.repositoryName(repoName)).imageIds();

        assertThat(imageIds).isEmpty();
    }

    @Test
    @Order(4)
    @Disabled
    void shouldPushImage() throws Exception {
        DockerClient dockerClient = DockerClientFactory.instance().client();

        // Pull a small base image
        dockerClient.pullImageCmd(SOURCE_IMAGE)
                .exec(new ResultCallback.Adapter<PullResponseItem>())
                .awaitCompletion(120, TimeUnit.SECONDS);

        // Tag it for the ECR registry
        String imageId = dockerClient.inspectImageCmd(SOURCE_IMAGE).exec().getId();
        dockerClient.tagImageCmd(imageId, ecrImageName, SOURCE_IMAGE_TAG).exec();

        // Push to ECR
        dockerClient.pushImageCmd(ecrImageRef)
                .withAuthConfig(authConfig)
                .exec(new ResultCallback.Adapter<PushResponseItem>())
                .awaitCompletion(120, TimeUnit.SECONDS);
    }

    @Test
    @Order(5)
    @Disabled
    void shouldListPushedImage() {
        List<ImageIdentifier> imageIds = ecr.listImages(b -> b.repositoryName(repoName)).imageIds();

        assertThat(imageIds).isNotEmpty();
        assertThat(imageIds).anyMatch(id -> SOURCE_IMAGE_TAG.equals(id.imageTag()));
    }

    @Test
    @Order(6)
    @Disabled
    void shouldPullImage() throws Exception {
        DockerClient dockerClient = DockerClientFactory.instance().client();

        // Remove the local tagged image
        dockerClient.removeImageCmd(ecrImageRef).exec();

        // Pull it back from ECR
        dockerClient.pullImageCmd(ecrImageRef)
                .withAuthConfig(authConfig)
                .exec(new ResultCallback.Adapter<PullResponseItem>())
                .awaitCompletion(120, TimeUnit.SECONDS);

        // Verify the image was pulled successfully
        var inspectResponse = dockerClient.inspectImageCmd(ecrImageRef).exec();
        assertThat(inspectResponse).isNotNull();
        assertThat(inspectResponse.getId()).isNotBlank();

        // Clean up local image
        dockerClient.removeImageCmd(ecrImageRef).exec();
    }

    @Test
    @Order(7)
    void shouldDeleteRepository() {
        ecr.deleteRepository(b -> b.repositoryName(repoName).force(true));

        List<String> repoNames = ecr.describeRepositories(b -> {}).repositories().stream()
                .map(Repository::repositoryName)
                .toList();

        assertThat(repoNames).doesNotContain(repoName);
    }
}
