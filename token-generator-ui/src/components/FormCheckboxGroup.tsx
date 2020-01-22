import React, {FormEvent} from 'react';
import {Form} from 'semantic-ui-react';
import './FormRadioGroup.css';

type Props = {
    label: string;
    values: Array<string>;
    setValue: (e: FormEvent<HTMLInputElement>,
               value: string | number | undefined,
               checked: boolean | undefined) => void;
    data: Array<string>;
};

const FormCheckboxGroup: React.FC<Props> = ({
                                                label,
                                                values,
                                                setValue,
                                                data,
                                            }: Props) => (
    <>
        <label>{label}</label>
        <Form.Group widths="three" className="Group">
            {data.map((v) => (
                <Form.Checkbox
                    key={v}
                    label={v}
                    value={v}
                    checked={values.includes(v)}
                    onChange={(e, {value, checked}) => setValue(e, value, checked)}
                />
            ))}
        </Form.Group>
    </>
);

export default FormCheckboxGroup;
