//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.domain.bo.divide;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AverageDivideStrategy extends DivideStrategy{


    public AverageDivideStrategy(PackAlgorithm algorithm) {
        super(algorithm);
    }

    @Override
    protected int gotPackageSize(Integer total, Integer upperLimit) {
        int num = (total / upperLimit);
        if (total % upperLimit > 0){
            num += 1;
        }
        return total / num;
    }

}
