# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4] - 2021-02-16 
### Added
- Proper support for 1.13+ Items (eg `BLUE_WOOL`) / legacy items (eg `WOOL/3`) added everywhere (I think)
- `/privatemines reset` to reset your own mine (permission: `privatemines.reset`)
- `/privatemines reset <player>` to reset another player's mine (permission: `privatemines.reset.other`)
- An option to toggle whether NPCs should spawn or not (`NPC-Enabled: true/false`)
- A few new messages
### Fixed
- On legacy versions, block checking when pasting a mine should now work
- Legacy material literals are now parsed correctly
- Item names in the change block menu should now be prettier
- A few subtle bugs in `PrivateMinesCommand`
### Changed
- The entire mine is now removed upon deletion to avoid overlap
- A few code things (update shadowJar, change nullity annotations, add some test cases for material parsing, etc)

## [1.3.2] - 2020-12-27
### Fixed
- Fix bug where spawn locations in mines weren't detected properly
- Jar Size somewhat reduced

## [1.3.2] - 2020-12-27
### Fixed
- No longer throws an error on startup

## [1.3] - 2020-12-27
### Added
- PrivateMines now supports 1.13 - 1.16+ Material names
### Changed 
- When running `/pm` without a mine, a message will be sent rather than opening the `/pm list` menu
- All plugin integrations except WorldEdit/WorldGuard are now optional
- The default world name has been renamed from `mines` to `privatemines`
### Removed
- An unused key from the config (`Mine-Schematic`)
### Fixed
- Fix a bug where mines never reset
- Fix a bug where flags for regions weren't properly set
