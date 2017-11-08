# ATOS RFI location

Adds a location for ATOS RFI project. 

It's an OSGi  bundle which starts a jax-ws server `RFILocationServer` built with CXF


## TODO

- support multi-tenancy in `RFIServerPortTypeImpl`
- implement `release` in `RFILocation`
- test from AMP 4.8 and 5
- fix `RFILocationLiveTest`

## How to run it

See `RFILocationMockTest` as reference