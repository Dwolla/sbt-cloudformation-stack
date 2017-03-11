# Amazon Web Services CloudFormation Stack SBT Plugin

[![Travis](https://img.shields.io/travis/Dwolla/sbt-cloudformation-stack.svg?style=flat-square)](https://travis-ci.org/Dwolla/sbt-cloudformation-stack)
[![Bintray](https://img.shields.io/bintray/v/dwolla/sbt-plugins/sbt-cloudformation-stack.svg?style=flat-square)](https://bintray.com/dwolla/sbt-plugins/sbt-cloudformation-stack/view)
[![license](https://img.shields.io/github/license/Dwolla/sbt-cloudformation-stack.svg?style=flat-square)]()

An sbt plugin to deploy a CloudFormation stack generated by the project. Create a main class in the project that accepts a file path as its first argument, and write the stack's CloudFormation template to the given file.

## Installation and Enabling

Add the following to `project/plugins.sbt`

    addSbtPlugin("com.dwolla.sbt" % "sbt-cloudformation-stack" % "{version-number}")

    resolvers ++= Seq(
      Resolver.bintrayIvyRepo("dwolla", "sbt-plugins"),
      Resolver.bintrayIvyRepo("dwolla", "maven")
    )

Then, enable the plugin by adding something like the following to `build.sbt`:

    val app = (project in file(".")).enablePlugins(CloudFormationStack)

## Settings

### `templateJsonFilename`

Filename of the generated JSON CloudFormation template.

### `stackName`

Name of the stack. Defaults the normalized name of the project.

### `stackParameters`

List of key-value pairs that will be provided to the stack as parameters. These can accept task or setting values; e.g., S3 bucket and keys from the [S3 Publisher](https://github.com/Dwolla/sbt-s3-publisher) plugin.

    stackParameters ++= List(
      "S3Bucket" → s3Bucket.value,
      "S3Key" → s3Key.value
    )

## Tasks

### `generate`

Runs the main class of the project, passing `templateJson` as the first argument. 

### `deploy`

Creates or updates a CloudFormation stack using the generated JSON template.

## Commands

### `withDeployParameters`

Allows certain deploy parameters to be set on the command line, with some input validation. The parameters that will be overridden are as follows:

- `awsAccountId` must be a 12-digit AWS Account ID
- `awsRoleName` must match the pattern `role/{name}`, and the value will be set to `{name}`
- `stackRoleArn` will be set to a full ARN if `awsAccountId` and `awsRoleName` are set, or to `None` if either are missing
- `deployEnvironment` must be one of the values in `deployEnvironmentOptions`, and will add a stack parameter named using `deployEnvironmentParameterName`.

    The default `deployEnvironmentParameterName` is `Environment`, so setting `deployEnvironment` to `Admin` would add a stack parameter named `Environment` with the value `Admin`.

Note that all these will be set if the command is invoked. Missing values will be set to `None`. The parameters can be passed in any order.

For example,

```ShellSession
sbt "withDeployParameters 123456789012 role/myRole Admin" deploy
```

will add a stack parameter `Environment → Admin` and create or update the stack using the AWS role `arn:aws:iam::123456789012:role/myRole`.
