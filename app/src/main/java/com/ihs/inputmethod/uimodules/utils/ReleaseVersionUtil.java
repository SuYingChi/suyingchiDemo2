package com.ihs.inputmethod.uimodules.utils;

/**
 * Created by wenbinduan on 2016/11/18.
 */

public final class ReleaseVersionUtil {

	/**
	 *
	 * @param version
	 * @param release
	 * @return true release >= version ;false release < version
	 */
	public static boolean compareReleaseVersion(final String version, final String release) {

		final String[] versions=version.split("\\.");
		final String[] releases=release.split("\\.");

		for(int i=0;i<versions.length&&i<releases.length;i++){

			if(Integer.parseInt(releases[i])<Integer.parseInt(versions[i])){
				return false;
			}else if(Integer.parseInt(releases[i])>Integer.parseInt(versions[i])){
				return true;
			}
		}

		//in case like 6.0 vs 6.0.1
		if(releases.length<versions.length){
			for(int i=releases.length;i<versions.length;i++){
				if(Integer.parseInt(versions[i])>0){
					return false;
				}
			}
		}
		return true;
	}
}
