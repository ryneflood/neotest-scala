{
  description = "Scala development environment";

  inputs = { nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable"; };

  outputs = { self, nixpkgs }:
    let pkgs = nixpkgs.legacyPackages.x86_64-linux.pkgs;
    in {
      devShells.x86_64-linux.default = pkgs.mkShell {
        name = "Default shell";
        buildInputs = (with pkgs; [ jdk17 scala-cli bloop metals mill ]);
        shellHook = ''
          echo "Entering Scala development environment"
        '';
      };
    };
}
