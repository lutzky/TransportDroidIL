TransportDroidIL
================

TransportDroidIL is a neat little android app for getting bus & train times for
Israel. It was written by Ohad Lutzky <ohad@lutzky.net>, with many patches
by Haggai Eran <haggai.eran@gmail.com>.

The app itself is in the `TransportDroidIL` project. The `TransportDroidILTest`
project contains functioning unit-tests (yay!), which must be in a separate
project in the Android SDK.

How it works
------------

TransportDroidIL receives user input and sends it to either
[Egged](http://egged.co.il) or the [Ministry of Transport](http://bus.gov.il)
sites, using the free-text engine there. Since the sites are nearly identical
in implementation, it's not a lot of extra work. Any information reported by
the application comes from those two sites, so we support whatever queries they
do. Also, for convenience, there's a history-based autocompleter.

License
-------

The source code for TransportDroidIL is available under the GPL license, as
available in the `COPYING` file. TransportDroidIL is also available for free
download.

Some icons are from: http://glyphish.com (CC)
Otherse are from: http://www.androidicons.com (CC)
