# App Customization

The app allows some ways to override besides the usual way of adjusting configurations. Clone customization repository into the `app-customizations`. In this example we use the
SURF version, it can be replaced with your own version of this repository with customizations

```shell
git clone git@github.com:OpenConext/OpenConext-myconext-custom-surf.git app-customizations
```

This `app-customizations` folder will contain overrides for client and server. The example above is the actual implementation of the SURF version of the overrides, your own git
repo can be used here. For the client there is a `translations` folder to override a translation per key, only the translations that need to be different from the default ones can
be specified here. The `myCustomApp.tsx` contains an object that allows to override certain feature in the app, for example providing an alternative app logo. To apply the
overrides a symlink needs to be applied, see below. Run the commands from the repository root. The symlink targets are relative to the
location of the link itself (not the current directory), hence the `../` segments. Restart the client dev server and the server after creating the symlinks.

## Client

Symlink to client

```shell
ln -s ../../../app-customizations/client login-gui/src/appCustomizations/myCustomizations
```

Removing symlink

```shell
rm login-gui/src/appCustomizations/myCustomizations
```
