import { Component } from 'preact';
import style from './style';

export default class Form extends Component {

	// gets called when this route is navigated to
	componentDidMount() {
		let myform = document.forms.myform;
		myform.saml.value = 'nindfsd';
		myform.submit();
	}

	render() {
		return (
			<div class={style.profile}>

				<p>Submitting form.</p>
				<form id="myform" action="https://google.com" method="post">
					<input name="saml" type="text" hidden value="test" />
					<input name="nice" type="text" hidden value="test" />
				</form>
			</div>
		);
	}
}
