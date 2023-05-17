import {useEffect, useState} from 'react';
import {Button, Input, InputNumber, Switch} from 'antd';
import {CheckOutlined, CloseOutlined, PlusSquareOutlined, RestOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';

const SLOValue = ({expression, metricType, onChange}) => {
  const [values, setValues] = useState([]);

  useEffect(() => {
    onChange?.(values);
  }, [values]);

  useEffect(() => {
    setValues(['']);
  }, [expression]);

  const onChangeValue = (newValue, valueIdx) => {
    setValues((prevValues) => prevValues.map((value, idx) => {
      if (valueIdx === idx) {
        return newValue;
      }
      return value;
    }));
  };

  const onClickAddValue = () => {
    setValues((prevValues) => [...prevValues, '']);
  };

  const onClickDeleteValue = (idx) => {
    setValues((prevValues) => [...prevValues.slice(0, idx), ...prevValues.slice(idx + 1, prevValues.length)]);
  };


  const getInput = (value, idx, isArray) => {
    const selectAfter = isArray && metricType !== 'boolean' ?
      <Button className="inline" type="text" icon={<RestOutlined />} disabled={values.length <= 1} onClick={() => onClickDeleteValue(idx)}/> :
      null;

    let input;
    switch (metricType) {
      case 'number':
        input = <InputNumber
          className="w-full"
          key={idx}
          controls={false}
          value={value}
          onChange={(value) => onChangeValue(value, idx)}
          addonAfter={selectAfter}
        />;
        break;
      case 'boolean':
        input =<Switch
          className="w-full"
          key={idx}
          checkedChildren={<CheckOutlined />}
          unCheckedChildren={<CloseOutlined />}
          defaultChecked
          checked={value}
          onChange={(value) => onChangeValue(value, idx)}
        />;
        break;
      default:
        input = <Input
          className="w-full"
          key={idx}
          value={value}
          onChange={(e) => onChangeValue(e.target.value, idx)}
          addonAfter={selectAfter}
        />;
    }
    return input;
  };

  if (expression === '') {
    return <div className="block w-52" />;
  } else if (expression === '==' && metricType !== 'boolean') {
    return <div className="block w-52 text-right">
      {values.map((value, idx) => {
        return getInput(value, idx, true);
      })}
      <Button type="default" icon={<PlusSquareOutlined />} className="relative left-auto right-0" size="small" onClick={onClickAddValue}>Value</Button>
    </div>;
  } else {
    return <div className="block w-52 text-right">{getInput(values[0], 0, false)}</div>;
  }
};

SLOValue.propTypes = {
  expression: PropTypes.string.isRequired,
  metricType: PropTypes.string.isRequired,
  onChange: PropTypes.func,
};

export default SLOValue;
