package com.gpufilter.helper;


import com.gpufilter.basefilter.GPUImageFilter;
import com.gpufilter.filter.MagicAntiqueFilter;
import com.gpufilter.filter.MagicBrannanFilter;
import com.gpufilter.filter.MagicCoolFilter;
import com.gpufilter.filter.MagicFreudFilter;
import com.gpufilter.filter.MagicHefeFilter;
import com.gpufilter.filter.MagicHudsonFilter;
import com.gpufilter.filter.MagicInkwellFilter;
import com.gpufilter.filter.MagicN1977Filter;
import com.gpufilter.filter.MagicNashvilleFilter;

public class MagicFilterFactory {

    private static MagicFilterType filterType = MagicFilterType.NONE;

    public static GPUImageFilter initFilters(MagicFilterType type) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case ANTIQUE:
                return new MagicAntiqueFilter();
            case BRANNAN:
                return new MagicBrannanFilter();
            case FREUD:
                return new MagicFreudFilter();
            case HEFE:
                return new MagicHefeFilter();
            case HUDSON:
                return new MagicHudsonFilter();
            case INKWELL:
                return new MagicInkwellFilter();
            case N1977:
                return new MagicN1977Filter();
            case NASHVILLE:
                return new MagicNashvilleFilter();
            case COOL:
                return new MagicCoolFilter();
            case WARM:
                return new MagicWarmFilter();
            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }

    private static class MagicWarmFilter extends GPUImageFilter {
    }
}
