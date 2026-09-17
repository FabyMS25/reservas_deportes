# Permission flow

Login validates the identity in the backend. For staff, the backend obtains the assigned permissions from Usuarios. For taxpayers, it builds the menu from `src/main/resources/security/taxpayer-menu.json`, including the representative-only actions when the stored organization relationship applies.

Monthly-payment creation and updating are available in the taxpayer menu without requiring representation. The stored student/tutor and school/representative relationships still determine which enrollments can be used. A taxpayer without active students in their care or in a represented school receives the no-students message. Student creation is representative-only for taxpayers: the server requires both their own responsibility and a school they represent. Authorized staff can assign a non-representative parent as tutor in any school within their permitted scope.

`permission-resources.json` maps each upstream permission UUID and action to a stable resource route. This is necessary because different modules share action routes such as `/reservations/details`. Display names are not authorization keys. Renaming a menu does not require changing the helpers, controller policies, or this identity mapping. New upstream permission identities need a mapping; unknown identities grant no access.

The backend returns the menu with `permissionRoute` metadata and signs the resolved permission snapshot in the Deportes token. The frontend saves the response in sessionStorage. Its helper reads this cached menu to decide which controls to display; it makes no HTTP requests. The backend independently checks the signed action and, for record mutations, the stored owner, representative relationship, or staff assignment. Editing sessionStorage cannot change the signed permissions.

There is one taxpayer display-name definition per module in the backend JSON. The unused frontend menu definition was removed. Staff display names continue to come from Usuarios. Configuration behavior and storage remain unchanged. The existing administrator detection rule also remains unchanged.

Deploy both applications together. Permission format version 2 requires users with earlier tokens to log in again. The token lifetime remains configured by the existing Deportes token settings; permission additions require a new login. This is a snapshot, not immediate permission revocation. Usuarios and RUAT tokens remain server-side integrations.

Maintenance: automated test sources and runner configuration have been removed at the project owner's request. Use the Angular build and Gradle bootJar for compilation/package checks, then verify staff, ordinary taxpayer and representative flows manually. Compilation does not establish live end-to-end behavior against Usuarios, RUAT or the deployed database.
