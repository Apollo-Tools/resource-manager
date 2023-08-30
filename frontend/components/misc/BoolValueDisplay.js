import {CheckCircleTwoTone, CloseCircleTwoTone} from '@ant-design/icons';
import {ICON_GREEN, ICON_RED} from './Constants';
import PropTypes from 'prop-types';

const BoolValueDisplay = ({value}) => {
  if (value) {
    return <CheckCircleTwoTone twoToneColor={ICON_GREEN}/>;
  } else {
    return <CloseCircleTwoTone twoToneColor={ICON_RED}/>;
  }
};

BoolValueDisplay.propTypes = {
  value: PropTypes.bool.isRequired,
};
export default BoolValueDisplay;
