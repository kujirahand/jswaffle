#!/usr/bin/php
<?php

$src_dir = "./jsWaffleTpl";
$des_dir = "./jsWaffleForAndroid/framework";

// remove
exec("rm -f -r $des_dir");
mkdir($des_dir, 0777);
//
exec("rm -f $src_dir/bin/*");
exec("rm -f -r $src_dir/bin/classes");
exec("rm -f -r $src_dir/bin/res");
//
r_copy("/");

function r_copy($target) {
  global $src_dir, $des_dir;
  $dir = $src_dir.$target;
  $desdir = $des_dir.$target;
  if (!file_exists($desdir)) {
    mkdir($desdir);
  }
  $h = opendir($dir);
  while (($file = readdir($h)) !== false) {
    if ($file == "." || $file == "..") continue;
    if ($file == ".svn-base") continue;
    if ($file == ".svn") continue;
    $src = $dir.$file;
    if (is_dir($src)) {
      r_copy($target.$file."/");
      continue;
    }
    $des = $des_dir.$target.$file;
    echo "-$src\n+$des\n";
    //echo "-$target.$file\n";
    copy($src, $des);    
  }
  closedir($h);
}




