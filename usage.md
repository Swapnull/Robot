#Usage

All the code to run the given tests are provided at the bottom of the submitted document. They are commented out so they do not evaluate when loading up the REPL.

A lein project.clj has been included, the packages from there will need to be installed.

To run custom commands, the run function can be used with

(run {packagesToCollect} {packagesToDeliver}) or 
(run {packagesToCollect} {packagesToDeliver} start)

The first command uses "office" as the start by default, so this can be omitted. 

An example command would be

(run [["r101" "r115"]] []) ;;Moves from office to collect package at r101. From r101 collects the package and delivers to r115. With no packages left to deliver, moves back to office.
(run [["r103" "b2"] ["r101" "r115"]] []) ;; Collects packages from r101 and r103, delivering to r115 and b2 respectively. Then returns to office

(run [] ["r115"]) ;; No packages to collect, only one to deliver to r115
(run [["r111" "r121"]] ["r121" "storage"]);; packages to collect from r111 and deliver to r121. Also packages to deliver from office to r121 and storage.

Output will be given in the command line.
