* `./gradlew prepareForRelease` (In case stop releasing after running this command, make sure removing `.travis/release`)
* Add CHANGELOG.md to what changes for new version
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
* `git add .;git commit -m "Version bump";git push origin master`
* Check [travis ci](https://travis-ci.org/permissions-dispatcher/permissions-dispatcher-plugin) to successfully release the module
* Create Release Tag (Upload archive file as well)
