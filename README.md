# Chunk Loader

ArctisChunkLoader is a custom Minecraft plugin that allows players and administrators to force-load chunks through an intuitive GUI. 
It ensures that specific chunks stay loaded even when no players are nearby, which is perfect for keeping farms, redstone contraptions, or automated processes running continuously.

## Features
- **Interactive GUI**: Easy-to-use graphical user interface for managing chunk loaders.
- **Persistent Force-Loading**: Selected chunks will remain loaded across server restarts.
- **Chunk Renaming**: Players can rename their loaded chunks for easier management.
- **Admin & Player Controls**: Separate permissions for regular usage and administrative management.
- **Configurable limits**: Set custom limits per-player.
- **Chunk Border Notifications**: Notifies players when crossing boundaries of loaded chunks.
- **Admin Notifications**: Notifies admins when a chunk is loaded or unloaded.

## Requirements
- **Minecraft Version**: 1.21
- **Java Version**: 21
- **Server API**: Spigot/Paper

## Installation
1. Download the latest `ArctisChunkLoader.jar` from the release section or build it using Gradle.
2. Drop the `.jar` file into your server's `plugins/` directory.
3. Restart your server.
4. (Optional) Configure LuckPerms to assign proper permissions to your players.

## Commands
* `/chunkloader` (Aliases: `/cl`) - Opens the main ChunkLoader GUI.

## Permissions (LuckPerms)
You can use a permissions plugin like [LuckPerms](https://luckperms.net/) to assign the following permission nodes:

| Permission Node | Default | Description |
| :--- | :--- | :--- |
| `chunkloader.use` | `true` | Allows a player to open the GUI and manage their own chunk loaders. |
| `chunkloader.admin` | `op` | Grants administrative access to manage all chunk loaders on the server. |

## Configuration
In `plugins/ArctisChunkLoader/config.yml`, you can configure:
- `user-chunk-limit`: Limit the amount of chunks a regular user is able to force-load (Default: 5).
- `admin-notifications`: Enable or disable admin chat notifications for created or deleted chunks (Default: true).

## Building the Plugin
To build the plugin from the source code, make sure you have Java 21 installed.
Run the following command in the project's root directory:

```bash
./gradlew build
```
The compiled `.jar` file will be located in the `build/libs/` directory.
