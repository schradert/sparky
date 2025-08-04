{ pkgs, ... }:
{
  android.enable = true;
  android.android-studio.enable = true;
  claude.code.permissions.WebFetch.allow = [
    "domain:github.com"
    "domain:kotlinlang.org"
    "domain:developer.android.com"
    "domain:docs.anthropic.com"
  ];
  claude.code.permissions.Bash.allow = [
    "rg:*"
    "nix search:*"
    "nix-instantiate:*"
  ];
  languages.java.enable = true;
  languages.java.jdk.package = pkgs.openjdk17;
  languages.kotlin.enable = true;
  languages.nix.enable = true;
  packages = with pkgs; [ claude-code ];
  scripts = {
    # Build commands
    build-android.exec = "gradle :android:assembleDebug";
    build-shared.exec = "gradle :shared:build";
    build-ios.exec = "gradle :shared:embedAndSignAppleFrameworkForXcode";
    run-android.exec = "gradle :android:installDebug && adb shell am start -n com.sparky.inventory.android/.MainActivity";

    # Core Testing Commands
    test.exec = "gradle :shared:test";
    test-unit.exec = "gradle :shared:testDebugUnitTest";
    test-release.exec = "gradle :shared:testReleaseUnitTest";
    test-android.exec = "gradle :android:testDebugUnitTest";
    test-coverage.exec = "gradle :shared:testDebugUnitTestCoverage";
    test-watch.exec = "gradle :shared:test --continuous";

    # Quality Assurance Commands
    lint.exec = "gradle :shared:lint :android:lint";
    lint-fix.exec = "gradle :shared:lintFix && ktlint --format 'shared/src/**/*.kt' 'android/src/**/*.kt'";
  };

  git-hooks.default_stages = [
    "pre-push"
    "manual"
  ];
  git-hooks.hooks = {
    ktlint = {
      enable = true;
      name = "ktlint";
      description = "Anti-bikeshedding Kotlin linter";
      files = "\\.kts?$";
      entry = "${pkgs.ktlint}/bin/ktlint --format";
    };

    # pre-commit builtins
    check-added-large-files.enable = true;
    check-case-conflicts.enable = true;
    check-executables-have-shebangs.enable = true;
    check-merge-conflicts.enable = true;
    check-symlinks.enable = true;
    check-vcs-permalinks.enable = true;
    end-of-file-fixer.enable = true;
    fix-byte-order-marker.enable = true;
    forbid-new-submodules.enable = true;
    mixed-line-endings.enable = true;
    no-commit-to-branch.enable = true;
    no-commit-to-branch.settings.branch = [
      "main"
      "trunk"
    ];
    trim-trailing-whitespace.enable = true;

    # third-party
    commitizen.enable = true;
    gitleaks = {
      enable = true;
      name = "gitleaks";
      description = "Gitleaks on entire project";
      entry = "${pkgs.gitleaks}/bin/gitleaks protect --redact";
    };
    markdownlint.enable = true;
    markdownlint.settings.configuration.MD013.line_length = -1;
    tagref.enable = true;
    typos.enable = true;

    # nix
    alejandra.enable = true;
    deadnix.enable = true;
    statix.enable = true;
    statix.raw.args = [
      "--config"
      ((pkgs.formats.toml { }).generate "statix.toml" {
        disabled = [
          "unquoted_uri"
          "repeated_keys"
        ];
      })
    ];
  };
}
