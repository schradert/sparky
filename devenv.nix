{ lib, pkgs, ... }:
{
  imports = [
    (lib.mkIf pkgs.stdenv.isDarwin {
      languages.swift.enable = true;
      git-hooks.hooks = {
        swiftlint = {
          enable = true;
          name = "SwiftLint";
          description = "Enforcing Swift style and conventions";
          files = "\\.swift$";
          entry = lib.getExe pkgs.swiftlint;
        };
        swiftformat = {
          enable = true;
          name = "SwiftFormat";
          description = "Formatting Swift code with conventional style";
          files = "\\.swift$";
          entry = lib.getExe pkgs.swiftformat;
        };
      };
    })
  ];
  android = {
    enable = true;
    android-studio.enable = true;
    # TODO derive from gradle/libs.versions.toml
    platforms.version = ["35"];
  };
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
  packages = with pkgs; [ claude-code google-cloud-sdk ];
  scripts.run-android.exec = "gradle :composeApp:installDebugAndroid && adb shell am start -n com.sparkysballoons.invx/.MainActivity";

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
    no-commit-to-branch.settings.branch = ["trunk"];
    trim-trailing-whitespace.enable = true;

    # third-party
    commitizen.enable = true;
    gitleaks = {
      enable = true;
      name = "gitleaks";
      description = "Gitleaks on entire project";
      entry = "${pkgs.gitleaks}/bin/gitleaks protect --redact";
    };
    lychee.enable = true;
    markdownlint.enable = true;
    markdownlint.settings.configuration.MD013.line_length = -1;
    mdsh.enable = true;
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
