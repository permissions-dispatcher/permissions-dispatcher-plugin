* Add CHANGELOG.md to what changes for new version
* Delete -SNAPSHOT from build.gralde (e.g. `version '1.0.3-SNAPSHOT'` -> `version '1.0.3'`)
* Add plugin.xml change note
```xml
    <change-notes><![CDATA[
        <p>1.0.1</p>
        <ul>
            <li>Fix issue #2 invalid revision range</li>
            <li>Fix issue #5 show error/info notifications</li>
        </ul>
    ]]>
    </change-notes>
```
* `touch .travis/release`
* Commit & push changes
* Create Release Tag (Upload archive file as well)
* Check [travis ci](https://travis-ci.org/shiraji/find-pull-request) to successfully release the module
* `rm .travis/release`
* Prepare for next version (e.g. `version '1.0.3'` -> `version '1.0.4-SNAPSHOT'`)
