package neotest

import munit.*

class ScalatestTestParserSuite extends munit.FunSuite:
  val xmlOutput =
    """|<?xml version="1.0" encoding="UTF-8" ?>
    |<testsuite errors="0" failures="2" hostname="fat-tony" name="foo.bar.FooSuite" tests="3" time="0.08" timestamp="2024-12-15T16:57:53">
    |  <properties>
    |    <property name="java.runtime.name" value="OpenJDK Runtime Environment"/>
    |    <property name="java.vm.version" value="21.0.3+9-nixos"/>
    |    <property
    |    name="sun.boot.library.path" value="/nix/store/gj4kkk14sld1p38m438dy3c903s4i6ll-openjdk-21.0.3+9/lib/openjdk/lib"/>
    |    <property name="java.vm.vendor" value="Oracle Corporation"/>
    |    <property name="java.vendor.url" value="https://openjdk.org/"/>
    |    <property name="path.separator" value=":"/>
    |    <property name="java.vm.name" value="OpenJDK 64-Bit Server VM"/>
    |    <property name="user.country" value="US"/>
    |    <property name="sun.java.launcher" value="SUN_STANDARD"/>
    |    <property
    |    name="java.vm.specification.name" value="Java Virtual Machine Specification"/>
    |    <property name="user.dir" value="/home/ryne/.local/share/scalacli/bloop"/>
    |    <property
    |    name="bloop.truncate-output-file-periodically" value="/home/ryne/.local/share/scalacli/bloop/daemon/output"/>
    |    <property name="java.runtime.version" value="21.0.3+9-nixos"/>
    |    <property name="os.arch" value="amd64"/>
    |    <property name="java.io.tmpdir" value="/tmp"/>
    |    <property name="line.separator" value=" "/>
    |    <property name="java.vm.specification.vendor" value="Oracle Corporation"/>
    |    <property name="stderr.encoding" value="UTF-8"/>
    |    <property name="os.name" value="Linux"/>
    |    <property name="sun.jnu.encoding" value="UTF-8"/>
    |    <property name="stdout.encoding" value="UTF-8"/>
    |    <property
    |    name="java.library.path" value="/nix/store/pbvn150v0w5v1jadv1b8wnvkp81vxz5k-pipewire-1.2.6-jack/lib:/usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib"/>
    |    <property name="jdk.debug" value="release"/>
    |    <property name="java.class.version" value="65.0"/>
    |    <property
    |    name="java.specification.name" value="Java Platform API Specification"/>
    |    <property
    |    name="sun.management.compiler" value="HotSpot 64-Bit Tiered Compilers"/>
    |    <property name="bloop.ignore-sig-int" value="true"/>
    |    <property name="os.version" value="6.12.2-zen1"/>
    |    <property name="user.home" value="/home/ryne"/>
    |    <property name="user.timezone" value="America/Toronto"/>
    |    <property name="file.encoding" value="UTF-8"/>
    |    <property name="java.specification.version" value="21"/>
    |    <property name="user.name" value="ryne"/>
    |    <property
    |    name="java.class.path" value="/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/bloop-frontend_2.12/2.0.0/bloop-frontend_2.12-2.0.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.19/scala-library-2.12.19.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/bloop-shared_2.12/2.0.0/bloop-shared_2.12-2.0.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/bloop-backend_2.12/2.0.0/bloop-backend_2.12-2.0.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scalaz/scalaz-core_2.12/7.3.8/scalaz-core_2.12-7.3.8.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix_2.12/3.2.0/monix_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/alexarchambault/case-app_2.12/2.0.6/case-app_2.12-2.0.6.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/scala-debug-adapter_2.12/4.2.0/scala-debug-adapter_2.12-4.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/bloop-config_2.12/2.0.3/bloop-config_2.12-2.0.3.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.3.14/logback-classic-1.3.14.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/libdaemon_2.12/0.0.12/libdaemon_2.12-0.0.12.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/plokhotnyuk/jsoniter-scala/jsoniter-scala-core_2.12/2.13.3.2/jsoniter-scala-core_2.12-2.13.3.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/plokhotnyuk/jsoniter-scala/jsoniter-scala-macros_2.12/2.13.3.2/jsoniter-scala-macros_2.12-2.13.3.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/bsp4s_2.12/2.1.1/bsp4s_2.12-2.1.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc_2.12/1.10.1/zinc_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.23.0/log4j-core-2.23.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/net/jpountz/lz4/lz4/1.3.0/lz4-1.3.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/dev/dirs/directories/26/directories-26.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/get-coursier/interface/1.0.19/interface-1.0.19.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/test-interface/1.0/test-interface-1.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/test-agent/1.10.1/test-agent-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/googlecode/java-diff-utils/diffutils/1.3.0/diffutils-1.3.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/pprint_2.12/0.9.0/pprint_2.12-0.9.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/github/alexarchambault/bleep/nailgun-server/1.0.7/nailgun-server-1.0.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/librarymanagement-ivy_2.12/1.10.1/librarymanagement-ivy_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/sourcecode_2.12/0.4.2/sourcecode_2.12-0.4.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/directory-watcher/0.8.0%2B6-f651bd93/directory-watcher-0.8.0%2B6-f651bd93.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/zeroturnaround/zt-zip/1.17/zt-zip-1.17.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/zipkin/brave/brave/5.18.1/brave-5.18.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/zipkin/reporter2/zipkin-sender-urlconnection/2.17.2/zipkin-sender-urlconnection-2.17.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm/9.7/asm-9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-util/9.7/asm-util-9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix-execution_2.12/3.2.0/monix-execution_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix-catnap_2.12/3.2.0/monix-catnap_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix-eval_2.12/3.2.0/monix-eval_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix-tail_2.12/3.2.0/monix-tail_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix-reactive_2.12/3.2.0/monix-reactive_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/monix-java_2.12/3.2.0/monix-java_2.12-3.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/alexarchambault/case-app-annotations_2.12/2.0.6/case-app-annotations_2.12-2.0.6.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/alexarchambault/case-app-util_2.12/2.0.6/case-app-util_2.12-2.0.6.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/epfl/scala/com-microsoft-java-debug-core/0.34.0%2B31/com-microsoft-java-debug-core-0.34.0%2B31.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.12.19/scala-reflect-2.12.19.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scalameta/parsers_2.12/4.9.7/parsers_2.12-4.9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-collection-compat_2.12/2.12.0/scala-collection-compat_2.12-2.12.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/unroll-annotation_2.12/0.1.12/unroll-annotation_2.12-0.1.12.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/ch/qos/logback/logback-core/1.3.14/logback-core-1.3.14.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/me/vican/jorge/jsonrpc4s_2.12/0.1.0/jsonrpc4s_2.12-0.1.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-core_2.12/1.10.1/zinc-core_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-persist_2.12/1.10.1/zinc-persist_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-compile-core_2.12/1.10.1/zinc-compile-core_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-classfile_2.12/1.10.1/zinc-classfile_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.23.0/log4j-api-2.23.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lihaoyi/fansi_2.12/0.5.0/fansi_2.12-0.5.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/librarymanagement-core_2.12/1.10.1/librarymanagement-core_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/sjson-new-core_2.12/0.9.1/sjson-new-core_2.12-0.9.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/ivy/ivy/2.3.0-sbt-396a783bba347016e7fe30dacc60d355be607fe2/ivy-2.3.0-sbt-396a783bba347016e7fe30dacc60d355be607fe2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/zipkin/reporter2/zipkin-reporter-brave/2.17.2/zipkin-reporter-brave-2.17.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/zipkin/zipkin2/zipkin/2.27.0/zipkin-2.27.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/zipkin/reporter2/zipkin-reporter/2.17.2/zipkin-reporter-2.17.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.7/asm-tree-9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/ow2/asm/asm-analysis/9.7/asm-analysis-9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/monix/implicitbox_2.12/0.2.0/implicitbox_2.12-0.2.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jctools/jctools-core/2.1.2/jctools-core-2.1.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-effect_2.12/2.1.3/cats-effect_2.12-2.1.3.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/chuusai/shapeless_2.12/2.3.3/shapeless_2.12-2.3.3.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/io/reactivex/rxjava2/rxjava/2.2.21/rxjava-2.2.21.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/commons-io/commons-io/2.16.1/commons-io-2.16.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scalameta/trees_2.12/4.9.7/trees_2.12-4.9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/outr/scribe_2.12/3.5.5/scribe_2.12-3.5.5.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-apiinfo_2.12/1.10.1/zinc-apiinfo_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-classpath_2.12/1.10.1/zinc-classpath_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/compiler-interface/1.10.1/compiler-interface-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/zinc-persist-core-assembly/1.10.1/zinc-persist-core-assembly-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/io_2.12/1.10.0/io_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/util-logging_2.12/1.10.0/util-logging_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/util-relation_2.12/1.10.0/util-relation_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/sbinary_2.12/0.5.1/sbinary_2.12-0.5.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-xml_2.12/2.3.0/scala-xml_2.12-2.3.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/launcher-interface/1.4.2/launcher-interface-1.4.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/modules/scala-parser-combinators_2.12/1.1.2/scala-parser-combinators_2.12-1.1.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/net/openhft/zero-allocation-hashing/0.16/zero-allocation-hashing-0.16.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/util-control_2.12/1.10.0/util-control_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-compiler/2.12.19/scala-compiler-2.12.19.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/github/mwiede/jsch/0.2.17/jsch-0.2.17.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/gigahorse-apache-http_2.12/0.7.0/gigahorse-apache-http_2.12-0.7.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/util-position_2.12/1.10.0/util-position_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/util-cache_2.12/1.10.0/util-cache_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-core_2.12/2.1.1/cats-core_2.12-2.1.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/macro-compat_2.12/1.1.1/macro-compat_2.12-1.1.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/google/errorprone/error_prone_annotations/2.27.0/error_prone_annotations-2.27.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scalameta/common_2.12/4.9.7/common_2.12-4.9.7.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/outr/perfolation_2.12/1.2.8/perfolation_2.12-1.2.8.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/outr/moduload_2.12/1.1.5/moduload_2.12-1.1.5.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/compiler-bridge_2.12/1.10.1/compiler-bridge_2.12-1.10.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/util-interface/1.10.0/util-interface-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/swoval/file-tree-views/2.1.12/file-tree-views-2.1.12.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/collections_2.12/1.10.0/collections_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/core-macros_2.12/1.10.0/core-macros_2.12-1.10.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/scala-sbt/jline/jline/2.14.7-sbt-9c3b6aca11c57e339441442bbf58e550cdfecb79/jline-2.14.7-sbt-9c3b6aca11c57e339441442bbf58e550cdfecb79.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jline/jline-terminal/3.24.1/jline-terminal-3.24.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jline/jline-terminal-jna/3.24.1/jline-terminal-jna-3.24.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jline/jline-terminal-jansi/3.24.1/jline-terminal-jansi-3.24.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/lmax/disruptor/3.4.2/disruptor-3.4.2.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/sjson-new-scalajson_2.12/0.9.1/sjson-new-scalajson_2.12-0.9.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/gigahorse-core_2.12/0.7.0/gigahorse-core_2.12-0.7.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/shaded-apache-httpasyncclient/0.7.0/shaded-apache-httpasyncclient-0.7.0.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/sjson-new-murmurhash_2.12/0.9.1/sjson-new-murmurhash_2.12-0.9.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-macros_2.12/2.1.1/cats-macros_2.12-2.1.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/typelevel/cats-kernel_2.12/2.1.1/cats-kernel_2.12-2.1.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/thesamet/scalapb/scalapb-runtime_2.12/0.11.17/scalapb-runtime_2.12-0.11.17.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/fusesource/jansi/jansi/2.4.1/jansi-2.4.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/org/jline/jline-native/3.24.1/jline-native-3.24.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/shaded-jawn-parser_2.12/0.9.1/shaded-jawn-parser_2.12-0.9.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/eed3si9n/shaded-scalajson_2.12/1.0.0-M4/shaded-scalajson_2.12-1.0.0-M4.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/ssl-config-core_2.12/0.6.1/ssl-config-core_2.12-0.6.1.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/thesamet/scalapb/lenses_2.12/0.11.17/lenses_2.12-0.11.17.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/google/protobuf/protobuf-java/3.19.6/protobuf-java-3.19.6.jar:/home/ryne/.cache/coursier/v1/https/repo1.maven.org/maven2/com/typesafe/config/1.4.2/config-1.4.2.jar"/>
    |    <property name="java.vm.specification.version" value="21"/>
    |    <property name="sun.arch.data.model" value="64"/>
    |    <property
    |    name="sun.java.command" value="bloop.BloopServer daemon:/home/ryne/.local/share/scalacli/bloop/daemon"/>
    |    <property
    |    name="java.home" value="/nix/store/gj4kkk14sld1p38m438dy3c903s4i6ll-openjdk-21.0.3+9/lib/openjdk"/>
    |    <property name="user.language" value="en"/>
    |    <property name="java.specification.vendor" value="Oracle Corporation"/>
    |    <property name="java.vm.info" value="mixed mode, sharing"/>
    |    <property name="java.version" value="21.0.3"/>
    |    <property name="native.encoding" value="UTF-8"/>
    |    <property name="java.vendor" value="N/A"/>
    |    <property name="file.separator" value="/"/>
    |    <property name="java.version.date" value="2024-04-16"/>
    |    <property
    |    name="java.vendor.url.bug" value="https://bugreport.java.com/bugreport/"/>
    |    <property name="sun.io.unicode.encoding" value="UnicodeLittle"/>
    |    <property name="sun.cpu.endian" value="little"/>
    |  </properties>
    |  <testcase 
    |  name="Foo Suite Bar Suite Foo" classname="foo.bar.FooSuite" time="0.03">
    |    <failure 
    |    message="true did not equal false" type="class org.scalatest.exceptions.TestFailedException">
    |      org.scalatest.exceptions.TestFailedException: true did not equal false
    |      at org.scalatest.matchers.MatchersHelper$.indicateFailure(MatchersHelper.scala:407)
    |      at org.scalatest.matchers.should.Matchers$ShouldMethodHelperClass.shouldMatcher(Matchers.scala:6778)
    |      at org.scalatest.matchers.should.Matchers.should(Matchers.scala:6835)
    |      at org.scalatest.matchers.should.Matchers.should$(Matchers.scala:1808)
    |      at org.scalatest.matchers.should.Matchers$.should(Matchers.scala:7692)
    |      at foo.bar.FooSuite.testFun$proxy1$1(TestSuite.scala:15)
    |      at foo.bar.FooSuite.fun$proxy1$1$$anonfun$1(TestSuite.scala:14)
    |      at org.scalatest.funspec.AnyFunSpecLike.org$scalatest$funspec$AnyFunSpecLike$ItWord$$_$applyImpl$$anonfun$1(AnyFunSpecLike.scala:158)
    |      at org.scalatest.Transformer.apply$$anonfun$1(Transformer.scala:22)
    |      at org.scalatest.OutcomeOf.outcomeOf(OutcomeOf.scala:85)
    |      at org.scalatest.OutcomeOf.outcomeOf$(OutcomeOf.scala:31)
    |      at org.scalatest.OutcomeOf$.outcomeOf(OutcomeOf.scala:104)
    |      at org.scalatest.Transformer.apply(Transformer.scala:22)
    |      at org.scalatest.Transformer.apply(Transformer.scala:21)
    |      at org.scalatest.funspec.AnyFunSpecLike$$anon$1.apply(AnyFunSpecLike.scala:481)
    |      at org.scalatest.TestSuite.withFixture(TestSuite.scala:196)
    |      at org.scalatest.TestSuite.withFixture$(TestSuite.scala:138)
    |      at org.scalatest.funspec.AnyFunSpec.withFixture(AnyFunSpec.scala:1631)
    |      at org.scalatest.funspec.AnyFunSpecLike.invokeWithFixture$1(AnyFunSpecLike.scala:487)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTest$$anonfun$1(AnyFunSpecLike.scala:491)
    |      at org.scalatest.SuperEngine.runTestImpl(Engine.scala:306)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTest(AnyFunSpecLike.scala:491)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTest$(AnyFunSpecLike.scala:49)
    |      at org.scalatest.funspec.AnyFunSpec.runTest(AnyFunSpec.scala:1631)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTests$$anonfun$1(AnyFunSpecLike.scala:524)
    |      at org.scalatest.SuperEngine.traverseSubNodes$2$$anonfun$1(Engine.scala:413)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
    |      at scala.collection.immutable.List.foreach(List.scala:334)
    |      at org.scalatest.SuperEngine.traverseSubNodes$1(Engine.scala:429)
    |      at org.scalatest.SuperEngine.runTestsInBranch(Engine.scala:390)
    |      at org.scalatest.SuperEngine.traverseSubNodes$2$$anonfun$1(Engine.scala:427)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
    |      at scala.collection.immutable.List.foreach(List.scala:334)
    |      at org.scalatest.SuperEngine.traverseSubNodes$1(Engine.scala:429)
    |      at org.scalatest.SuperEngine.runTestsInBranch(Engine.scala:390)
    |      at org.scalatest.SuperEngine.traverseSubNodes$2$$anonfun$1(Engine.scala:427)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
    |      at scala.collection.immutable.List.foreach(List.scala:334)
    |      at org.scalatest.SuperEngine.traverseSubNodes$1(Engine.scala:429)
    |      at org.scalatest.SuperEngine.runTestsInBranch(Engine.scala:396)
    |      at org.scalatest.SuperEngine.runTestsImpl(Engine.scala:475)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTests(AnyFunSpecLike.scala:524)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTests$(AnyFunSpecLike.scala:49)
    |      at org.scalatest.funspec.AnyFunSpec.runTests(AnyFunSpec.scala:1631)
    |      at org.scalatest.Suite.run(Suite.scala:1112)
    |      at org.scalatest.Suite.run$(Suite.scala:563)
    |      at org.scalatest.funspec.AnyFunSpec.org$scalatest$funspec$AnyFunSpecLike$$super$run(AnyFunSpec.scala:1631)
    |      at org.scalatest.funspec.AnyFunSpecLike.run$$anonfun$1(AnyFunSpecLike.scala:528)
    |      at org.scalatest.SuperEngine.runImpl(Engine.scala:535)
    |      at org.scalatest.funspec.AnyFunSpecLike.run(AnyFunSpecLike.scala:528)
    |      at org.scalatest.funspec.AnyFunSpecLike.run$(AnyFunSpecLike.scala:49)
    |      at org.scalatest.funspec.AnyFunSpec.run(AnyFunSpec.scala:1631)
    |      at org.scalatest.tools.Framework.org$scalatest$tools$Framework$$runSuite(Framework.scala:318)
    |      at org.scalatest.tools.Framework$ScalaTestTask.execute(Framework.scala:513)
    |      at sbt.ForkMain$Run.lambda$runTest$1(ForkMain.java:413)
    |      at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
    |      at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
    |      at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
    |      at java.base/java.lang.Thread.run(Thread.java:840)
    |</failure>
    |</testcase>
    |  <testcase 
    |  name="Foo Suite Bar Suite Bar" classname="foo.bar.FooSuite" time="0.001">
    |    <failure 
    |    message="true did not equal false" type="class org.scalatest.exceptions.TestFailedException">
    |      org.scalatest.exceptions.TestFailedException: true did not equal false
    |      at org.scalatest.matchers.MatchersHelper$.indicateFailure(MatchersHelper.scala:407)
    |      at org.scalatest.matchers.should.Matchers$ShouldMethodHelperClass.shouldMatcher(Matchers.scala:6778)
    |      at org.scalatest.matchers.should.Matchers.should(Matchers.scala:6835)
    |      at org.scalatest.matchers.should.Matchers.should$(Matchers.scala:1808)
    |      at org.scalatest.matchers.should.Matchers$.should(Matchers.scala:7692)
    |      at foo.bar.FooSuite.testFun$proxy2$1(TestSuite.scala:18)
    |      at foo.bar.FooSuite.fun$proxy1$1$$anonfun$2(TestSuite.scala:17)
    |      at org.scalatest.funspec.AnyFunSpecLike.org$scalatest$funspec$AnyFunSpecLike$ItWord$$_$applyImpl$$anonfun$1(AnyFunSpecLike.scala:158)
    |      at org.scalatest.Transformer.apply$$anonfun$1(Transformer.scala:22)
    |      at org.scalatest.OutcomeOf.outcomeOf(OutcomeOf.scala:85)
    |      at org.scalatest.OutcomeOf.outcomeOf$(OutcomeOf.scala:31)
    |      at org.scalatest.OutcomeOf$.outcomeOf(OutcomeOf.scala:104)
    |      at org.scalatest.Transformer.apply(Transformer.scala:22)
    |      at org.scalatest.Transformer.apply(Transformer.scala:21)
    |      at org.scalatest.funspec.AnyFunSpecLike$$anon$1.apply(AnyFunSpecLike.scala:481)
    |      at org.scalatest.TestSuite.withFixture(TestSuite.scala:196)
    |      at org.scalatest.TestSuite.withFixture$(TestSuite.scala:138)
    |      at org.scalatest.funspec.AnyFunSpec.withFixture(AnyFunSpec.scala:1631)
    |      at org.scalatest.funspec.AnyFunSpecLike.invokeWithFixture$1(AnyFunSpecLike.scala:487)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTest$$anonfun$1(AnyFunSpecLike.scala:491)
    |      at org.scalatest.SuperEngine.runTestImpl(Engine.scala:306)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTest(AnyFunSpecLike.scala:491)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTest$(AnyFunSpecLike.scala:49)
    |      at org.scalatest.funspec.AnyFunSpec.runTest(AnyFunSpec.scala:1631)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTests$$anonfun$1(AnyFunSpecLike.scala:524)
    |      at org.scalatest.SuperEngine.traverseSubNodes$2$$anonfun$1(Engine.scala:413)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
    |      at scala.collection.immutable.List.foreach(List.scala:334)
    |      at org.scalatest.SuperEngine.traverseSubNodes$1(Engine.scala:429)
    |      at org.scalatest.SuperEngine.runTestsInBranch(Engine.scala:390)
    |      at org.scalatest.SuperEngine.traverseSubNodes$2$$anonfun$1(Engine.scala:427)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
    |      at scala.collection.immutable.List.foreach(List.scala:334)
    |      at org.scalatest.SuperEngine.traverseSubNodes$1(Engine.scala:429)
    |      at org.scalatest.SuperEngine.runTestsInBranch(Engine.scala:390)
    |      at org.scalatest.SuperEngine.traverseSubNodes$2$$anonfun$1(Engine.scala:427)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
    |      at scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
    |      at scala.collection.immutable.List.foreach(List.scala:334)
    |      at org.scalatest.SuperEngine.traverseSubNodes$1(Engine.scala:429)
    |      at org.scalatest.SuperEngine.runTestsInBranch(Engine.scala:396)
    |      at org.scalatest.SuperEngine.runTestsImpl(Engine.scala:475)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTests(AnyFunSpecLike.scala:524)
    |      at org.scalatest.funspec.AnyFunSpecLike.runTests$(AnyFunSpecLike.scala:49)
    |      at org.scalatest.funspec.AnyFunSpec.runTests(AnyFunSpec.scala:1631)
    |      at org.scalatest.Suite.run(Suite.scala:1112)
    |      at org.scalatest.Suite.run$(Suite.scala:563)
    |      at org.scalatest.funspec.AnyFunSpec.org$scalatest$funspec$AnyFunSpecLike$$super$run(AnyFunSpec.scala:1631)
    |      at org.scalatest.funspec.AnyFunSpecLike.run$$anonfun$1(AnyFunSpecLike.scala:528)
    |      at org.scalatest.SuperEngine.runImpl(Engine.scala:535)
    |      at org.scalatest.funspec.AnyFunSpecLike.run(AnyFunSpecLike.scala:528)
    |      at org.scalatest.funspec.AnyFunSpecLike.run$(AnyFunSpecLike.scala:49)
    |      at org.scalatest.funspec.AnyFunSpec.run(AnyFunSpec.scala:1631)
    |      at org.scalatest.tools.Framework.org$scalatest$tools$Framework$$runSuite(Framework.scala:318)
    |      at org.scalatest.tools.Framework$ScalaTestTask.execute(Framework.scala:513)
    |      at sbt.ForkMain$Run.lambda$runTest$1(ForkMain.java:413)
    |      at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
    |      at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
    |      at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
    |      at java.base/java.lang.Thread.run(Thread.java:840)
    |</failure>
    |</testcase>
    |  <testcase name="Baz Suite Baz" classname="foo.bar.FooSuite" time="0.001">
    |  </testcase>
    |  <system-out/>
    |  <system-err/>
    |</testsuite>""".stripMargin

  val simpleTestOutput = """|
    |Compiling baz.test (1 Scala source)
    |Compiled baz.test (494ms)
    |[32mFooSuite:[0m
    |[32mFoo Suite[0m
    |[32m- Foo[0m
    |[32m- Bar[0m
    |Execution took 9ms
    |2 tests, 2 passed
    |All tests in foo.bar.FooSuite passed
    |
    |===============================================
    |Total duration: 9ms
    |All 1 test suites passed.
    |===============================================
    |The test execution was successfully closed.""".stripMargin

  val basic = """
    |[32mFooSuite:[0m
    |[32mFoo Suite[0m
    |[32m  Bar Suite[0m
    |[32m    Again[0m
    |[32m    - Baz[0m
    |[32m    - Not Baz[0m
    |[32m- Foo[0m
    |[32m- Bar[0m
    |[32mAnother Suite[0m
    |[32m- Another Test[0m
  """.stripMargin

  val suiteWithFailedTest = """|
    |[32mFoo Suite[0m
    |[32m  Bar Suite[0m
    |[32m    Again[0m
    |[31m    - Baz *** FAILED ***[0m
    |[31m      true did not equal false (TestSuite.scala:11)[0m
    |[32m    - Not Baz[0m
    |[32m- Foo[0m
    |[31m- Bar *** FAILED ***[0m
    |[31m  true did not equal false (TestSuite.scala:24)[0m
    |""".stripMargin

  val successfulTestOutput = """
    |[32mFooSuite:[0m [32mFoo Suite[0m [32m Bar Suite[0m [32m - Baz[0m
    |[32m - Not Baz[0m [32m- Foo[0m [32m- Bar[0m Execution took 7ms 4
    |tests, 4 passed All tests in foo.bar.FooSuite passed

    |The test execution was successfully closed.
    |=============================================== Total duration: 7ms All 1
    |test suites passed.
    |===============================================
  """.stripMargin

  val multiSuiteTestOutput =
     """|[32mFoo Suite[0m
        |[32m  Bar Suite[0m
        |[32m    Again[0m
        |Some logging
        |
        |            Multi-line test debug output
        |            Multi-line test debug output
        |            Multi-line test debug output
        |
        |Multi-line test debug output
        |        "
        |[31m    - Baz *** FAILED ***[0m
        |[31m      true did not equal false (TestSuite.scala:19)[0m
        |[32m    - Not Baz[0m
        |[32m- Foo[0m
        |[31m- Bar *** FAILED ***[0m
        |[31m  true did not equal false (TestSuite.scala:31)[0m
        |[32mBaz Suite[0m
        |[32m- Baz[0m
        |Execution took 29ms
        |5 tests, 3 passed, 2 failed
        |
        |===============================================
        |Total duration: 29ms
        |1 failed
        |
        |Failed:
        |- foo.bar.FooSuite:
        |The test execution was successfully closed.
        |  * Foo Suite Bar Suite Again Baz - true did not equal false
        |  * Foo Suite Bar - true did not equal false
        |===============================================
  """.stripMargin

  // test("should parse a simple, non-nested, successful test output".ignore):
  //   val result = ScalatestTestOutputParser.parseSingleTestSuite("FooSuite")(simpleTestOutput.linesIterator.toList)
  //
  //   val expected = List(
  //     TestSuite(
  //       "FooSuite", 
  //       List(
  //         TestResultWithOutput.Passed("FooSuite.Foo Suite.Foo"),
  //         TestResultWithOutput.Passed("FooSuite.Foo Suite.Bar")
  //       )
  //     )
  //   )
  //
  //   assertEquals(result, expected)

  // test("should parse test output for a single, simple, Test Suite"):
  //   val result = ScalatestTestOutputParser.parseTestOutput("FooSuite")(basic.linesIterator.toList.drop(1))
  //   
  //   val expected = List(
  //     TestSuite(
  //       "FooSuite", 
  //       List(
  //         TestResultWithOutput.Passed("FooSuite.Foo Suite.Foo"),
  //         TestResultWithOutput.Passed("FooSuite.Foo Suite.Bar"),
  //       )
  //     ),
  //     TestSuite(
  //       "FooSuite", 
  //       List(
  //         TestResultWithOutput.Passed("FooSuite.Another Suite.Another Test")
  //       )
  //     )
  //   )
  //
  //   assertEquals(result, expected)
  

  // test("find test locations"):
  //   val locations = ScalatestTestOutputParser.findTestLocations(basic.linesIterator.toList)
  //
  //   val obtainedNames = locations.map(_.name)
  //
  //   val expectedNames = List(
  //     "Foo Suite:",
  //     "Foo Suite",
  //     "Bar Suite",
  //     "Again",
  //     "Baz",
  //     "Not Baz",
  //     "Foo",
  //     "Bar",
  //     "Another Suite",
  //     "Another Test"
  //   )
  //
  //   assertEquals(obtainedNames, expectedNames)

  // test("should parse steps into a TestSuite"):
  //    val locations = List(
  //       Step.NoChange(0, "Foo Suite", "Foo Suite"),
  //       Step.Increment(0, "  Bar Suite", "Bar Suite"),
  //       Step.Increment(0, "    Again", "Again"),
  //       Step.NoChange(0, "    - Baz", "Baz"),
  //       Step.NoChange(0, "    - Not Baz", "Not Baz"),
  //       Step.Decrement(0, "- Foo", "Foo", 2),
  //       Step.NoChange(0, "- Bar", "Bar"),
  //      )
  //
  //    val expected = TestSuite(
  //      "FooSuite",
  //      List(
  //         TestResultWithOutput.Passed("Foo Suite.Bar Suite.Again.Baz"),
  //         TestResultWithOutput.Passed("Foo Suite.Bar Suite.Again.Not Baz"),
  //         TestResultWithOutput.Passed("Foo Suite.Foo"),
  //         TestResultWithOutput.Passed("Foo Suite.Bar"),
  //        )
  //      )
  //
  //    val obtained = ScalatestTestOutputParser.parseSteps(List.empty, locations)
  //    
  //    assertEquals(obtained, expected)

  // test("should parse steps into a TestSuite, including a failed test with its output"):
  //    val locations = List(
  //       Step.NoChange(0, "[32mFoo Suite[0m", "Foo Suite"),
  //       Step.Increment(1, "[32m  Bar Suite[0m", "Bar Suite"),
  //       Step.Increment(2, "[32m    Again[0m", "Again"),
  //       Step.NoChange(3, "[31m    - Baz *** FAILED ***[0m", "Baz"),
  //       Step.NoChange(5, "[32m    - Not Baz[0m", "Not Baz"),
  //       Step.Decrement(6, "[32m- Foo[0m", "Foo", 2),
  //       Step.NoChange(7, "[31m- Bar *** FAILED ***[0m", "Bar"),
  //      )
  //
  //    val expectedFailedTestOutput = List(
  //      "[31m    - Baz *** FAILED ***[0m",
  //      "[31m      true did not equal false (TestSuite.scala:11)[0m"
  //      )
  //
  //    val expectedFailedTestOutput2 = List(
  //       "[31m- Bar *** FAILED ***[0m",
  //       "[31m  true did not equal false (TestSuite.scala:24)[0m"
  //     )
  //
  //    val expected = TestSuite(
  //      "FooSuite",
  //      List(
  //         TestResultWithOutput.Failed("Foo Suite.Bar Suite.Again.Baz", expectedFailedTestOutput),
  //         TestResultWithOutput.Passed("Foo Suite.Bar Suite.Again.Not Baz"),
  //         TestResultWithOutput.Passed("Foo Suite.Foo"),
  //         TestResultWithOutput.Failed("Foo Suite.Bar", expectedFailedTestOutput2),
  //        )
  //      )
  //
  //    val obtained = ScalatestTestOutputParser.parseSteps(suiteWithFailedTest.linesIterator.toList, locations)
  //    
  //    assertEquals(obtained, expected)
  //
  // test("should parse Successful test output".ignore):
  //   val result = ScalatestTestOutputParser.parseTestOutput("FooSuite")(successfulTestOutput.linesIterator.toList)
  //
  //   val expected = List(
  //     TestSuite(
  //       "FooSuite", 
  //       List(
  //         TestResultWithOutput.Passed("Foo Suite.Bar Suite"),
  //         TestResultWithOutput.Passed("Foo Suite.Bar Suite.Baz"),
  //         TestResultWithOutput.Passed("Foo Suite.Bar Suite.Not Baz"),
  //         TestResultWithOutput.Passed("Foo Suite.Foo"),
  //         TestResultWithOutput.Passed("Foo Suite.Bar"),
  //       )
  //     )
  //   )
  //
  //   assertEquals(result, expected)
  //
  // test("should split multi-suite test output into separate suites"):
  //   // we only want to identify the top-level suites, so, in this case those are
  //   // FooSuite and Baz Suite
  //   val obtained = ScalatestTestOutputParser.identifyTestSuiteLocations(multiSuiteTestOutput.linesIterator.toList)
  //
  //   val expected = List(
  //       Step.NoChange(0, "[32mFoo Suite[0m", "Foo Suite"),
  //       Step.NoChange(17, "[32mBaz Suite[0m", "Baz Suite"),
  //   )
  //
  //   assertEquals(obtained, expected)

  test("should be able to parse XML output".only):
    val obtained = ScalatestXmlTestOutputParser.parseTestOutput("FooSuite")(xmlOutput.linesIterator.toList)

    val expected = List.empty

    assertEquals(obtained, expected)
    
