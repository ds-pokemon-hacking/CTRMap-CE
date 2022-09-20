# CTRMap Community Edition

CTRMap CE is a framework designed for editing primarily the Generation V Pokémon game files and code, with most editors and format support provided through external plug-in modules. As it stands, this repo only contains the editor core and CreativeStudio, whose real functionality is provided by their respective extensions. You can snatch the latest build in the [releases](https://github.com/kingdom-of-ds-hacking/CTRMap-CE/releases) tab, or compile it from source using NetBeans (make sure to clone with `--recursive` in order to fetch all submodules properly).

## Installing plug-ins

At the moment, plug-in modules can only be installed from the CTRMap Project Manager accessible from the main launcher. As a result, CreativeStudio plug-ins will only be loaded when it is launched from within the main editor. Proper support will be introduced once a separate installer is up.

## Developing plug-ins

For documentation about plug-in development, consult the JavaDoc of their respective base interfaces:

- For CTRMap: `ctrmap.editor.system.juliet.ICTRMapPlugin|CTRMapPluginInterface`
- For Game Adapters: `ctrmap.editor.system.juliet.IGameAdapterPlugin|GameAdapterRegistry`
- For WSFS: `ctrmap.editor.system.workspace.IWSFSPlugin|WSFS`
- For CreativeStudio: `ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin|NGCSJulietIface`
- For the Launcher: `ctrmap.Launc.IPlugin|JulietInterface`

## License

CTRMap is licensed under GPLv3, however, due to what its inherent affiliation with ROM hacking, it's not strictly free software: everything that it's used for shall hereby be subject to the [CFRU](https://github.com/Skeli789/Complete-Fire-Red-Upgrade) clause, which I've taken the liberty of copying right here for everyone's convenience. Note that this is by no means a legal requirement, and even if it was, I wouldn't care to enforce it, just keep in mind that breaking it isn't exactly the nicest way of saying thanks to someone who spent several years programming this software for your own enjoyment.

`By using this or any assets from this repository, you consent to never making money off your game (unless you have my explicit permission). That includes both pay-walls as well as optional donations (which includes ko-fi, Patreon, etc.). If you have a problem with this, feel free to send me a Discord message and I will give you my PayPal so you can pay me $100000 for the hundreds if not thousands of hours I poured into this for free (I don't actually want your money - I'm trying to make a point). Not to mention it's illegal to profit off of an IP you don't own.`