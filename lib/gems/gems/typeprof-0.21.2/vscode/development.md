# LSP client for vscode

Note: this is under development

## How to run

```
$ cd vscode
$ npm install
$ npx run tsc -p ./
$ cd ..
$ code --extensionDevelopmentPath=vscode/ .
```

Alternatively, you can do it in vscode itself.

```
$ cd vscode
$ code
```

And then press F5 to run another vscode with extension.

## How to release

```
$ npm run package
$ npx vsce publish
```

See also: https://code.visualstudio.com/api/working-with-extensions/publishing-extension