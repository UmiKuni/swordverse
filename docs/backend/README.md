# SwordVerse backend development guide

## mvnw.cmd command

Please run this in `.\game-server` directory.

### clean

```shell
.\mvnw.cmd clean
```

### compile

```shell
.\mvnw.cmd compile
```

Or

```shell
.\mvnw.cmd clean compile
```

### test

This command runs the tests.

```shell
.\mvnw.cmd test
```

### package

This command compiles and packages the project.

```shell
.\mvnw.cmd package
```

### format

The first command will check for code format violations. The second command will fix them.

```shell
.\mvnw.cmd spotless:check
.\mvnw.cmd spotless:apply
```

Or you can run `.\mvnw.cmd spotless` to format all files.

### install

```shell
.\mvnw.cmd install
```

### run

```shell
.\mvnw.cmd spring-boot:run
```
