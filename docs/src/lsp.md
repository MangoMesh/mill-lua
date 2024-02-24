# LSP support

mill-lua aims to integrate well enough with Sumneko's Lua LSP. This is done through
the LSP configuration file, which is adapted to be aware of the file structure
provided by mill-lua.

## Usage

As simple as executing:

```bash
mill -i example.writeLSP
```