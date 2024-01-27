import {useEffect, useState} from 'react';
import {Button, Input, InputNumber, Select, Switch} from 'antd';
import {CheckOutlined, CloseOutlined, PlusSquareOutlined, RestOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';

const SLOValue = ({expression, metricType, selectables, onChange}) => {
  const [values, setValues] = useState([]);

  useEffect(() => {
    onChange?.(values);
  }, [values]);

  useEffect(() => {
    if (metricType === 'boolean') {
      setValues([true]);
    } else {
      setValues([undefined]);
    }
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
    setValues((prevValues) => [...prevValues, null]);
  };

  const onClickDeleteValue = (idx) => {
    setValues((prevValues) => [...prevValues.slice(0, idx), ...prevValues.slice(idx + 1, prevValues.length)]);
  };


  const getInput = (value, idx, isArray) => {
    const selectAfter = isArray && metricType !== 'boolean' ?
      <Button className="h-[30.5px]" type={'text'} icon={<RestOutlined />}
        disabled={values.length <= 1} onClick={() => onClickDeleteValue(idx)}/> :
      null;

    let input;
    switch (metricType) {
      case 'number':
        input = <InputNumber
          className="w-full h-[32px]"
          key={idx}
          controls={false}
          value={value}
          onChange={(value) => onChangeValue(value, idx)}
          addonAfter={selectAfter}
        />;
        break;
      case 'boolean':
        input = <Switch
          className="w-full max-h-[32px]"
          key={idx}
          checkedChildren={<CheckOutlined />}
          unCheckedChildren={<CloseOutlined />}
          checked={value}
          onChange={(value) => onChangeValue(value, idx)}
        />;
        break;
      case 'selectable':
        input = <div className="w-full box-border flex ant-input-wrapper h-[32px]">
          <Select
            className="inline-block custom-select"
            key={idx}
            value={value}
            size="middle"
            placeholder={'Select a value'}
            onChange={(value) => onChangeValue(value, idx)}
          >
            {selectables.map((selectable) => (
              <Select.Option key={selectable.id} value={selectable.id}>
                {selectable.name}
              </Select.Option>
            ))}
          </Select>
          <div className="box-border border border-solid border-[#d9d9d9] bg-[#00000005] rounded-r-[6px] w-[54px] px-[11px]">
            {selectAfter}
          </div>
        </div>;
        break;
      default:
        input = <Input
          className="w-full h-[32px]"
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
  metricType: PropTypes.oneOf(['string', 'number', 'boolean', 'selectable']).isRequired,
  selectables: PropTypes.arrayOf(PropTypes.shape({id: PropTypes.number.isRequired, name: PropTypes.string.isRequired})),
  onChange: PropTypes.func,
};

export default SLOValue;
