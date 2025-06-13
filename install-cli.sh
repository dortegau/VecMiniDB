#!/bin/bash

# VecMiniDB CLI Installation Script
# Builds and installs the CLI tool for easy access

set -e

echo "🚀 Building VecMiniDB CLI..."
./mvnw clean package -DskipTests -q

echo "📦 Creating CLI executable..."
CLI_JAR="target/vecminidb-cli.jar"
INSTALL_DIR="$HOME/.local/bin"
CLI_SCRIPT="$INSTALL_DIR/vecminidb"

# Create install directory if it doesn't exist
mkdir -p "$INSTALL_DIR"

# Create the CLI script
cat > "$CLI_SCRIPT" << 'EOF'
#!/bin/bash
# VecMiniDB CLI wrapper script

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_FILE="$SCRIPT_DIR/vecminidb-cli.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: VecMiniDB CLI JAR not found at $JAR_FILE"
    echo "Please run install-cli.sh from the project directory"
    exit 1
fi

# Run the CLI with any passed arguments
exec java -jar "$JAR_FILE" "$@"
EOF

# Copy JAR to install directory
cp "$CLI_JAR" "$INSTALL_DIR/"

# Make script executable
chmod +x "$CLI_SCRIPT"

echo "✅ VecMiniDB CLI installed successfully!"
echo ""
echo "Usage:"
echo "  vecminidb                    # Start interactive REPL"
echo "  vecminidb my_database.vecdb  # Use specific database file"
echo ""
echo "Add $INSTALL_DIR to your PATH if not already there:"
echo "  echo 'export PATH=\"\$HOME/.local/bin:\$PATH\"' >> ~/.bashrc"
echo "  echo 'export PATH=\"\$HOME/.local/bin:\$PATH\"' >> ~/.zshrc"
echo ""
echo "Then restart your terminal or run:"
echo "  source ~/.bashrc  # or ~/.zshrc"
echo ""
echo "🎉 Ready to use VecMiniDB CLI!"