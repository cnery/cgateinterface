#!/bin/bash
echo "git.commit=`git rev-parse --short HEAD`" > git-status.properties
tag=`git describe --tags`
if [ $? == 0 ]; then
    echo "git.tag=$tag" >> git-status.properties
    echo "git.branch=" >> git-status.properties
else
    echo "git.tag=" >> git-status.properties
    echo "git.branch=`git branch 2>/dev/null| sed -n '/^\*/s/^\* //p'`" >> git-status.properties
fi
if git diff --quiet 2>/dev/null >&2; then
    echo "git.dirty=false" >> git-status.properties
else
    echo "git.dirty=true" >> git-status.properties
fi

