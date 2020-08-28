# subra-aem-flagapp

#### Steps For Release:

##### Stage 1 : R-5 -> (2020/8/10) - Cutoff will be R-3 -> (2020/8/12)
- Inform everyone, set cutoff

##### Stage 2 : After Cutoff -> R-2 -> (2020/8/13) - Release will be R -> (2020/8/15)
- At cutoff, merge `develop` to `release-candidate`. At this point don't allow any `Merge-Request` to `develop` 
- Create an up-version of `develop` and then new development for next release can go into this. (1.x.x-SNAPSHOT to - 2.0.0-SNAPSHOT)
- Deploy to Stage, verify changes
- In case of issues create `big-fix` branch and merge into `release-candidate`, then retest by one incremental version (1.x.x2-SNAPSHOT)

##### Stage 3 : Verified Release -> R-1 -> (2020/8/14) - Release will be R -> (2020/8/15)
- Once `release-candidate` is ready set version by removing `-SNAPSHOT`, then merge it to `release`
- Create `tag` with release version

##### Stage 4 : Release R -> (2020/8/15)
- Deploy this tag build to production