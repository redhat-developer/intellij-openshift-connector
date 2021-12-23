registry.svg: from https://rawgit.com/patternfly/patternfly-design/master/styles/icons/patternfly-svg-icons.zip

plus-solid.svg: blob:https://fontawesome.com/050091c3-f46f-4695-a80e-9b35bf0d4119
chevron-down.svg: blob:https://fontawesome.com/593f8ba3-c8a6-4bb2-9308-54054f47b59a

To make dark: sed -e 's/currentColor/#AFB1B3' file.svg >file-dark.svg
To make light: sed -e 's/currentColor/#6E6E6E' file.svg >file.svg
Then open file in GIMP, resize and export as PNG