# Github Report Generator

This tool enables generating change report for a set of github repositories. Such a report helps a team to get a high level
overview of work being done across multiple repositories.

```bash
$ java -jar github-reporter.jar --since "2 days" apache/openwhisk 
```

The generated report looks like below

```markdown
# apache/openwhisk

## Issues

### Created:
    * #4626 - Allow limiting DB bloat by excluding response from Activation record in some cases (tysonnorris)
    * #4621 - ubuntu_setup/all.sh fails on ElementaryOS (anandskumar)
    * #4620 - subjects view has legacy code (rabbah)

## Pull Requests

### Merged:
    * #4618 - fix: controller ambiguous task env setting (BillZong)

### Created:
    * #4619 - Add ansible deploy options (BillZong)
    * #4617 - Add kind &quot;unknown&quot; to fallback activations (jiangpengcheng)

### Closed:
    * #4622 - OW Router aka pepin PoC (mcdan)
    * #4573 - Publish OpenWhisk standalone jar to Github release (chetanmeh)

### Updated:
    * #4611 - Factor out subjects view name for identities (rabbah)
    * #4609 - Add namespace field to activation log (jiangpengcheng)
```

It supports multiple options

1. Connect to internal github deployment (via `--github-uri`)
2. Generate report for all repo in an org (via `--org apache --repo-prefix openwhisk`)
3. Render markdown or html report (`--html-mode`)
4. Pass access token for higher rate limits
5. Send mail (TODO)

```bash
$ java -jar build/libs/github-reporter-1.0-SNAPSHOT.jar -h
      --github-uri  <arg>    Github server uri. By default it refers to public
                             github. If your repo is on an internal enterprise
                             deployment then set this to the server url
      --html-mode            Render in HTML mode. By default report is rendered
                             in Markdown format
      --org  <arg>           Organization name. Reporter would find its
                             repositories and generate report for them. If any
                             prefix if provided then only those repo would be
                             selected
  -o, --out  <arg>           Output file path
      --repo-prefix  <arg>   Repo name prefix. If provided only repo whose name
                             start with the provided prefix would be used
  -s, --since  <arg>         Date since changes need tobe reported in yyyy-MM-dd
                             format. One can also provide duration like '4
                             days`, '1 month'
  -t, --token  <arg>         Github access token. See
                             https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line
  -h, --help                 Show help message

 trailing arguments:
  repo-names (not required)   List of repository names for which report needs to
                              be generated e.g. apache/openwhisk

Github change reporter
```
