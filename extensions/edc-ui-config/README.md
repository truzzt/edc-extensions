<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/sovity/edc-ui">
    <img src="https://raw.githubusercontent.com/sovity/edc-ui/main/src/assets/images/sovity_logo.svg" alt="Logo" width="300">
  </a>

<h3 align="center">EDC-Connector Extension:<br />EDC UI Extension Config</h3>

  <p align="center">
    <a href="https://github.com/sovity/edc-extensions/issues">Report Bug</a>
    ·
    <a href="https://github.com/sovity/edc-extensions/issues">Request Feature</a>
  </p>
</div>

## About this Extension

Our [EDC UI](https://github.com/sovity/edc-ui/) requires many configuration properties which exist in the EDC Backend.

This extension provides an endpoint in the data management API `/edc-ui-config` which allows our EDC UI to retrieve
additional `EDC_UI_` properties from the backend.

It will pass all config properties starting with `edc.ui.` in general.

It will build `EDC_UI_` properties from backend properties where possible.

This excludes the Data Management API Endpoint URL and API keys or other sensitive data.

## Why does this extension exist?

By not having to repeat ourselves when configuring the EDC UI, we save time and reduce the risk of errors.

This extension was created when we decided to add more information to the EDC UI, which would have required us to
update the EDC UI configuration in many places.

## License

Apache License 2.0 - see [LICENSE](../../LICENSE)

## Contact

sovity GmbH - contact@sovity.de 