import React from 'react';
import { Form } from 'semantic-ui-react';
import './FormRadioGroup.css';

type Props = {
  label: string;
  value: string | number | undefined;
  setValue: (value: string | number | undefined) => void;
  data: string[];
};

const FormRadioGroup: React.FC<Props> = ({
  label,
  value,
  setValue,
  data,
}: Props) => (
  <>
    <label>{label}</label>
    <Form.Group widths="three" className="Group">
      {data.map((v) => (
        <Form.Radio
          key={v}
          label={v}
          value={v}
          checked={value === v}
          onChange={(e, { value }) => setValue(value)}
        />
      ))}
    </Form.Group>
  </>
);

export default FormRadioGroup;
