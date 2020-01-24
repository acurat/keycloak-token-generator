import React, {Dispatch} from 'react';
import {Formik, FormikHelpers} from "formik";
import {Button, Form} from "semantic-ui-react";
import {isNumber, isString} from "../utils/globals";
import {Tokens} from "../utils/types";

interface Props {
    setTokens: Dispatch<Tokens | undefined>;
    setLoading: Dispatch<boolean>;
}

interface State {
}

interface FormValues {
    aud: string;
    iss: string;
    azp: string;
    sub: string;
    exp: number;

    [key: string]: any
}

interface Errors {
    [key: string]: string
}

class GenerateCustomTokens extends React.Component<Props, State> {
    state = {};

    initialValues: FormValues = {
        iss: 'token-generator',
        azp: 'your-app',
        aud: 'your-tests',
        sub: 'tester',
        exp: Date.now()
    };

    resetState = () => {
        this.setState({
            error: undefined,
        })
    };

    validate = (values: FormValues) => {
        const errors: Errors = {};
        Object.keys(values).forEach(key => {
            const value = values[key];
            try {
                JSON.parse(values[key])
            } catch (e) {
                if (!isString(value) && !isNumber(value)) {
                    errors[key] = 'Should be a String, Number or valid JSON';
                }
            }
        });
        return errors;
    };

    submit = async (values: FormValues, {setSubmitting}: FormikHelpers<FormValues>) => {

        const {setTokens} = this.props;
        try {
            setSubmitting(true);
            const response = await fetch(
                `/jwt`,
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(values)
                }
            ).then((response) => response.text());
            setTokens({ accessToken: response });
        } catch (e) {
            console.error(e);
            setTokens(undefined);
            this.setState({
                error: 'Error encountered',
            });
        } finally {
            setSubmitting(false);
        }
    };

    render() {
        return (
            <Formik
                initialValues={this.initialValues}
                validate={this.validate}
                onSubmit={this.submit}
            >
                {({
                      values,
                      errors,
                      touched,
                      handleChange,
                      handleBlur,
                      handleSubmit,
                      isSubmitting,

                  }) => (
                    <Form onSubmit={handleSubmit}>
                        {Object.keys(values).map((key: string) => {
                            return (<Form.Group inline key={key}>
                                <label key={`label-${key}`}>{key}</label>
                                <Form.Input
                                    style={{ paddingLeft: '35px'}}
                                    key={`input-${key}`}
                                    placeholder=""
                                    onChange={handleChange}
                                    onBlur={handleBlur}
                                    value={values[key]}
                                    name={key}
                                    width={16}
                                    error={
                                        errors[key] && touched[key] && {
                                            content: errors[key],
                                            pointing: 'above'
                                        }
                                    }
                                />
                            </Form.Group>)
                        })}
                        < Button type="submit" disabled={isSubmitting}>
                            Submit
                        </Button>
                    </Form>
                )}
            </Formik>
        );
    }
}

export default GenerateCustomTokens;
