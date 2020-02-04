import React from 'react';
import {Formik} from "formik";
import {Button, Form, Select} from "semantic-ui-react";

interface Props {
    onAdd: (values: NewClaim, shouldValidate?: boolean | undefined) => any;
}

export interface NewClaim {
    name: string;
    type: string;
}

const options = [
    {key: 's', text: 'String', value: 'string'},
    {key: 'n', text: 'Number', value: 'number'},
    {key: 'd', text: 'Date', value: 'date'},
    {key: 'j', text: 'JSON', value: 'object'},
];

const AddClaim: React.FC<Props> = ({onAdd}) => {

    return (
        <div>
            <h3>Add a new claim to the token</h3>
            <Formik
                initialValues={{
                    name: '',
                    type: 'string',
                }}
                validate={(values => {
                    const errors: any = {};
                    if (!values.name) {
                        errors.name = 'Required'
                    }
                    return errors;
                })}
                onSubmit={(values: NewClaim, {setSubmitting}) => {
                    setSubmitting(false);
                    onAdd(values);
                }}>
                {({
                      values,
                      handleSubmit,
                      handleChange,
                      handleBlur,
                      setFieldValue,
                      isSubmitting,
                      isValid,
                      isValidating
                  }) => (
                    <Form onSubmit={handleSubmit}>
                        <label htmlFor="name">Name of Claim</label>
                        <Form.Input id="name" name="name"
                                    onChange={handleChange}
                                    onBlur={handleBlur}
                                    value={values.name}
                                    placeholder="Ex. nickname"
                                    maxLength={30}/>

                        <label htmlFor="type">Data Type of Claim</label>
                        <Form.Field
                            id="type"
                            name="type"
                            control={Select}
                            options={options}
                            placeholder='Data Type'
                            value={values.type}
                            onChange={(e: React.ChangeEvent<any>, {name, value}: { name: string, value: string }) => setFieldValue(name, value)}
                            onBlur={handleBlur}
                        />

                        <Button type="submit" disabled={isSubmitting || isValidating || !isValid}>Add</Button>
                    </Form>
                )}
            </Formik>
        </div>
    )

};

export default AddClaim;
