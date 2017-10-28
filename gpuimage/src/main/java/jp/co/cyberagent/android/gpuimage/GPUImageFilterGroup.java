/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.util.TextureTransform;

/**
 * Resembles a filter that consists of multiple filters applied after each
 * other.
 */
public class GPUImageFilterGroup implements GPUImageBaseFilter {
    private List<GPUImageBaseFilter> mFilters = new ArrayList<>();

    public static GPUImageBaseFilter createFilter(List<GPUImageBaseFilter> filterList) {
        if (filterList == null || filterList.size() == 0) {
            return new GPUImageFilter();
        } else if (filterList.size() == 1) {
            return filterList.get(0);
        } else {
            return new GPUImageFilterGroup(filterList);
        }
    }

    public GPUImageFilterGroup() {
        this(null);
    }

    public GPUImageFilterGroup(List<GPUImageBaseFilter> filterList) {
        if (filterList != null) {
            mFilters.addAll(filterList);
        }
    }

    public void addFilter(GPUImageBaseFilter filter) {
        if (filter != null) {
            mFilters.add(filter);
        }
    }

    public int getFilterSize(){
        return mFilters.size();
    }

    @Override
    public void destroy() {
        for (GPUImageBaseFilter filter : mFilters) {
            filter.destroy();
        }
    }

    @Override
    public TextureInfo draw(TextureInfo textureInfo, TextureTransform textureTransform, int outputWidth, int outputHeight, GPUImage.ScaleType scaleType, boolean drawToTexture) {
        if (mFilters != null) {
            int size = mFilters.size();
            TextureInfo previousTexture = textureInfo;
            for (int i = 0; i < size; i++) {
                GPUImageBaseFilter filter = mFilters.get(i);
                boolean isNotLast = i < size - 1;
                if (isNotLast) {
                    previousTexture = filter.draw(previousTexture, TextureTransform.NONE,
                            previousTexture.getTextureWidth(), previousTexture.getTextureHeight(), scaleType, true);
                } else {
                    previousTexture = filter.draw(previousTexture, textureTransform, outputWidth, outputHeight, scaleType, drawToTexture);
                }
            }
            return previousTexture;
        } else {
            return null;
        }
    }

    @Override
    public boolean requireFeature(FilterFeature feature) {
        for (GPUImageBaseFilter filter : mFilters) {
            if (filter.requireFeature(feature)) {
                return true;
            }
        }
        return false;
    }
}
