# Permission flow

Login validates the identity in the backend. For staff, the backend obtains the assigned permissions from Usuarios. For taxpayers, it builds the menu from `src/main/resources/security/taxpayer-menu.json`, including the representative-only actions when the stored organization relationship applies.

`permission-resources.json` maps each upstream permission UUID and action to a stable resource route. This is necessary because different modules share action routes such as `/reservations/details`. Display names are not authorization keys. Renaming a menu does not require changing the helpers, controller policies, or this identity mapping. New upstream permission identities need a mapping; unknown identities grant no access.

The backend returns the menu with `permissionRoute` metadata and signs the resolved permission snapshot in the Deportes token. The frontend saves the response in sessionStorage. Its helper reads this cached menu to decide which controls to display; it makes no HTTP requests. The backend independently checks the signed action and, for record mutations, the stored owner, representative relationship, or staff assignment. Editing sessionStorage cannot change the signed permissions.

There is one taxpayer display-name definition per module in the backend JSON. The unused frontend menu definition was removed. Staff display names continue to come from Usuarios. Configuration behavior and storage remain unchanged. The existing administrator detection rule also remains unchanged.

Deploy both applications together. Permission format version 2 requires users with earlier tokens to log in again. The token lifetime remains configured by the existing Deportes token settings; permission additions require a new login. This is a snapshot, not immediate permission revocation. Usuarios and RUAT tokens remain server-side integrations.

Validation: Angular development build and Gradle compileJava, without running tests. These checks do not establish live end-to-end behavior against Usuarios, RUAT, or the deployed database. Verify staff, ordinary taxpayer, and representative login and reservation operations in the deployment environment before release.
