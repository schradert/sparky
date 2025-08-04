{ pkgs, ... }:
{
  android.enable = true;
  android.android-studio.enable = true;
  android.reactNative.enable = true;
  claude.code.permissions.WebFetch.allow = [
    "domain:github.com"
    "domain:reactnative.dev"
    "domain:docs.anthropic.com"
  ];
  claude.code.permissions.Bash.allow = [
    "rg:*"
    "nix search:*"
    "nix-instantiate:*"
  ];
  git-hooks.default_stages = [
    "pre-push"
    "manual"
  ];
  git-hooks.hooks = {
    biome.enable = true;

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
    no-commit-to-branch.settings.branch = [ "trunk" ];
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
  languages.javascript.bun = {
    enable = true;
    install.enable = true;
  };
  languages.nix.enable = true;
  languages.typescript.enable = true;
  packages = [ pkgs.claude-code ];
}
