[![Maven Central](https://img.shields.io/maven-central/v/io.github.ryntric/channels.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.ryntric/workers-core)
[![LICENSE](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/ryntric/channels-java/blob/master/LICENSE)
[![Maven Build](https://github.com/ryntric/channels-java/actions/workflows/build.yml/badge.svg)](https://github.com/ryntric/channels-java/actions/workflows/build.yml)
[![JMH Benchmark](https://github.com/ryntric/channels-java/actions/workflows/jmh.yml/badge.svg)](https://github.com/ryntric/channels-java/actions/workflows/jmh.yml)
[![JCStress Tests](https://github.com/ryntric/channels-java/actions/workflows/jcstress.yml/badge.svg)](https://github.com/ryntric/channels-java/actions/workflows/jcstress.yml)
[![Qodana](https://github.com/ryntric/channels-java/actions/workflows/qodana_code_quality.yml/badge.svg)](https://github.com/ryntric/channels-java/actions/workflows/qodana_code_quality.yml)
[![pages-build-deployment](https://github.com/ryntric/channels-java/actions/workflows/pages/pages-build-deployment/badge.svg)](https://github.com/ryntric/channels-java/actions/workflows/pages/pages-build-deployment)

#### It is a low-latency concurrency library designed around ring buffers, sequencers, and customizable wait strategies. It provides spsc, mpsc, spmc, mpmc channels along with batch and single-item publishing modes, to maximize throughput and minimize contention

---
**Build Environment requirements**
- JDK 11 or greater
- Apache Maven 3.9.11 or greater

---
To build and run jmh tests execute the following commands:
```shell
mvn -pl jmh -am clean install
java -jar -Xms2G -Xmx2G -XX:+AlwaysPreTouch -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC jmh/target/jmh-1.0-SNAPSHOT.jar
```

If you want to run with thread affinity execute the following command:
```shell
taskset -c <corerange> java -jar -Xms2G -Xmx2G -XX:+AlwaysPreTouch -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC jmh/target/jmh-1.0-SNAPSHOT.jar 
```


To build and run jcstress tests execute the following commands:
```shell
mvn -pl jcstress -am clean install
java -jar jcstress/target/jcstress-1.0-SNAPSHOT.jar 
```
---

Example of usage
--- 

```java
public class Main {
    private static final AtomicLong counter = new AtomicLong();
    private static final Event DUMMY = new Event();

    private static class Event {
    }

    public static void main(String[] args) {
        Channel<Event> channel = Channel.spsc(8192, ProducerWaitStrategyType.SPINNING, ConsumerWaitStrategyType.SPINNING);
        Consumer<Event> handler = event -> System.out.println("Event received: " + counter.getAndIncrement());

        new Thread(() -> {
            while (true) {
                channel.blockingReceive(2048, handler);
            }
        }).start();

        for (int i = 0; i < 15_000_000; i++) {
            channel.push(DUMMY);
        }
    }
}
```

---
## Benchmark

### Hardware information:

#### OS:

```text
NAME="Fedora Linux"
VERSION="42 (KDE Plasma Desktop Edition)"
RELEASE_TYPE=stable
ID=fedora
VERSION_ID=42
VERSION_CODENAME=""
PLATFORM_ID="platform:f42"
PRETTY_NAME="Fedora Linux 42 (KDE Plasma Desktop Edition)"
CPE_NAME="cpe:/o:fedoraproject:fedora:42"
SUPPORT_END=2026-05-13
VARIANT="KDE Plasma Desktop Edition"
VARIANT_ID=kde
```

#### CPU:

```text
Architecture:                x86_64
  CPU op-mode(s):            32-bit, 64-bit
  Address sizes:             48 bits physical, 48 bits virtual
  Byte Order:                Little Endian
CPU(s):                      12
  On-line CPU(s) list:       0-11
Vendor ID:                   AuthenticAMD
  Model name:                AMD Ryzen 9 5900X 12-Core Processor
    CPU family:              25
    Model:                   33
    Thread(s) per core:      1
    Core(s) per socket:      12
    Socket(s):               1
    Stepping:                2
    Frequency boost:         disabled
    CPU(s) scaling MHz:      61%
    CPU max MHz:             6291.9360
    CPU min MHz:             720.1620
    BogoMIPS:                9399.78
    Flags:                   fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ht syscall nx mmxext fxsr_opt pdpe1gb rdt
                             scp lm constant_tsc rep_good nopl xtopology nonstop_tsc cpuid extd_apicid aperfmperf rapl pni pclmulqdq monitor ssse3 fma cx16 sse4_1 sse4_
                             2 x2apic movbe popcnt aes xsave avx f16c rdrand lahf_lm cmp_legacy svm extapic cr8_legacy abm sse4a misalignsse 3dnowprefetch osvw ibs skin
                             it wdt tce topoext perfctr_core perfctr_nb bpext perfctr_llc mwaitx cat_l3 cdp_l3 hw_pstate ssbd mba ibrs ibpb stibp vmmcall fsgsbase bmi1 
                             avx2 smep bmi2 erms invpcid cqm rdt_a rdseed adx smap clflushopt clwb sha_ni xsaveopt xsavec xgetbv1 xsaves cqm_llc cqm_occup_llc cqm_mbm_t
                             otal cqm_mbm_local user_shstk clzero irperf xsaveerptr rdpru wbnoinvd arat npt lbrv svm_lock nrip_save tsc_scale vmcb_clean flushbyasid dec
                             odeassists pausefilter pfthreshold avic v_vmsave_vmload vgif v_spec_ctrl umip pku ospke vaes vpclmulqdq rdpid overflow_recov succor smca fs
                             rm debug_swap
Virtualization features:     
  Virtualization:            AMD-V
Caches (sum of all):         
  L1d:                       384 KiB (12 instances)
  L1i:                       384 KiB (12 instances)
  L2:                        6 MiB (12 instances)
  L3:                        64 MiB (2 instances)
NUMA:                        
  NUMA node(s):              1
  NUMA node0 CPU(s):         0-11
Vulnerabilities:             
  Gather data sampling:      Not affected
  Ghostwrite:                Not affected
  Indirect target selection: Not affected
  Itlb multihit:             Not affected
  L1tf:                      Not affected
  Mds:                       Not affected
  Meltdown:                  Not affected
  Mmio stale data:           Not affected
  Reg file data sampling:    Not affected
  Retbleed:                  Not affected
  Spec rstack overflow:      Mitigation; Safe RET
  Spec store bypass:         Mitigation; Speculative Store Bypass disabled via prctl
  Spectre v1:                Mitigation; usercopy/swapgs barriers and __user pointer sanitization
  Spectre v2:                Mitigation; Retpolines; IBPB conditional; IBRS_FW; STIBP disabled; RSB filling; PBRSB-eIBRS Not affected; BHI Not affected
  Srbds:                     Not affected
  Tsa:                       Mitigation; Clear CPU buffers
  Tsx async abort:           Not affected

```

**Results:**

| Benchmark                                     | Mode | Cnt | Producer | Consumer | Score          | Error           | Units |
|-----------------------------------------------|------|-----|----------|----------|----------------|-----------------|-------|
| MultiProducerMultiConsumerBatchItemPerfTest   | thrpt| 5   | 4        | 4        | 630389637.679  | ± 195350961.870 | ops/s |
| MultiProducerMultiConsumerSingleItemPerfTest  | thrpt| 5   | 4        | 4        | 14443268.801   | ± 4127728.673   | ops/s |
| MultiProducerSingleConsumerBatchItemPerfTest  | thrpt| 5   | 4        | 1        | 1006265833.688 | ± 1136450.559   | ops/s |
| MultiProducerSingleConsumerSingleItemPerfTest | thrpt| 5   | 4        | 1        | 42846226.263   | ± 44004.409     | ops/s |
| SingleProducerMultiConsumerBatchItemPerfTest  | thrpt| 5   | 1        | 4        | 1835065761.167 | ± 3860980.946   | ops/s |
| SingleProducerSingleConsumerBatchItemPerfTest | thrpt| 5   | 1        | 1        | 1869878781.366 | ± 869359.180    | ops/s |
| SingleProducerSingleConsumerSingleItemPerfTest| thrpt| 5   | 1        | 1        | 172879950.874  | ± 6082737.656   | ops/s |

