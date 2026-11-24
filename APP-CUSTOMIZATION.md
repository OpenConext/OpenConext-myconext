# App Customization

The app allows some ways to override besides the usual way of adjusting configurations. Clone customization repository into the `app-customizations`. In this example we use the
SURF version, it can be replaced with your own version of this repository with customizations

```shell
git clone git@github.com:OpenConext/OpenConext-myconext-custom-surf.git app-customizations
```

This `app-customizations` folder contains the login-gui and my-gui overrides. The example above is the actual implementation of the SURF version of the overrides, your own git
repo can be used here. The `login-gui` folder has a `translations` folder to override a translation per key, only the translations that need to be different from the default ones can
be specified here. The `myCustomApp.tsx` contains an object that allows to override certain feature in the app, for example providing an alternative app logo. To apply the
overrides a symlink needs to be applied, see below. Run the commands from the repository root. The symlink targets are relative to the
location of the link itself (not the current directory), hence the `../` segments. Restart the login-gui dev server after creating the symlink.

## login-gui

Symlink to login-gui

```shell
ln -sfn ../../../app-customizations/login-gui login-gui/src/appCustomizations/myCustomizations
```

Removing symlink

```shell
unlink login-gui/src/appCustomizations/myCustomizations
```

## my-gui

Symlink to my-gui

```shell
ln -sfn ../../../app-customizations/my-gui my-gui/src/appCustomizations/myCustomizations
```

Removing symlink

```shell
unlink my-gui/src/appCustomizations/myCustomizations
```
