# panther-job
* Simple and lightweight job system with priorities and job group.
* It can be used in Java 7 or later.
* It worked on J2SE, J2EE and dalvik(ART) on android.

## Features
* Fast and lightweight job scheduler.
* Prioritization in 5 stages(min, below normal, normal, above normal, max).
* Support grouping of Jobs.
  * It is possible to enable, disable and suspend a certain time grouped jobs at once.
  * The maximum number of groups is 31 (group id : 0 to 30)
* It has a mechanism to repeat a job.
  * Re-execute after a certain time.
  * Re-execute at exception occurrence.
* Work on single thread.
  * No parallel work supported.
* No serialize supported.

License
* This software is released under the MIT License.
* http://opensource.org/licenses/mit-license.php

# Manual
## create job class
    public abstract class InvokeApiJob extends Job {
        public InvokeApiJob(Builder builder) {
            super(builder);
        }
        @Overirde
        public boolean execute() {
            // do something.
            return true;
        }
    }

## create callback
    public class MyCallback extends Callback<MyJob> {
        public void success(MyJob sender) {
            // called when job.execute() return true.
        }
        public void fail(MyJob sender) {
            // called when job.execute() return false.
        }
        public void exception(MyJob sender, Exception e) {
            // called when job.execute() exception occored.
        }
    }

## register job
    MyJob myJob = new MyJob(
        Job.Builder.
           create(JobManager.PRIORITY_MIN).
           callback(new MyCallback().
           addGroupMask(MY_JOB_GROUP_0).
          addGroupMask(MY_JOB_GROUP_0));
    JobManager.instance().register(myJob);

After register, job scheduler run automatically.

## re-execute job
See ```net.noranuk0.job.RepeatedlyRunCallback```

## suspend, disable, and enable job group
suspend

`JobManager.instance().suspendGroup(MY_JOB_GROUP_ID, System.currentTimeMillis() + 5000 /*wake up time*/);`

disable, enable

    JobManager.instance().disalbeGroup(MY_JOB_GROUP_ID);
    JobManager.instance().enalbeGroup(MY_JOB_GROUP_ID);
