// Pre-changelog-generation hook for TriPSs/conventional-changelog-action.
// Prevents major version bumps — maps them to minor instead.
// Breaking changes (feat!:, BREAKING CHANGE) will result in a minor bump.
exports.preVersionGeneration = (version) => {
  const currentMajor = process.env.CURRENT_MAJOR;
  if (!currentMajor) return version;

  const parts = version.split('.');
  if (parts[0] !== currentMajor) {
    // Major bump detected — cap to minor bump of current major
    const currentMinor = Number.parseInt(process.env.CURRENT_MINOR || '0', 10);
    return `${currentMajor}.${currentMinor + 1}.0`;
  }
  return version;
};
