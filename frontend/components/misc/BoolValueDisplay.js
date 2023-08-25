import {CheckCircleTwoTone, CloseCircleTwoTone} from "@ant-design/icons";
import {ICON_GREEN, ICON_RED} from "./Constants";

const BoolValueDisplay = ({value}) => {
    if (value) {
        return <CheckCircleTwoTone twoToneColor={ICON_GREEN}/>;
    } else {
        return <CloseCircleTwoTone twoToneColor={ICON_RED}/>;
    }
}

export default BoolValueDisplay;