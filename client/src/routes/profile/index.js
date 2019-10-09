import { h, Component } from 'preact';
import style from './style';
import {user} from "../../api";

export default class Profile extends Component {
	state = {
		time: Date.now(),
		count: 10,
		user: {}
	};

	// update the current time
	updateTime = () => {
		this.setState({ time: Date.now() });
	};

	increment = () => {
		this.setState({ count: this.state.count+1 });
	};

	// gets called when this route is navigated to
	componentDidMount() {
		// start a timer for the clock:
		this.timer = setInterval(this.updateTime, 1000);
		user().then(json => this.setState({user: json}));
	}

	// gets called just before navigating away from the route
	componentWillUnmount() {
		clearInterval(this.timer);
	}

	// Note: `user` comes from the URL, courtesy of our router
	render({ name }, { time, count, user }) {
		return (
			<div class={style.profile}>
				<h1>Profile: {user.name}</h1>
				<p>This is the user profile for a user named { name }.</p>

				<p>This is the user profile for a user named { user.name }.</p>

				<div>Current time: {new Date(time).toLocaleString()}</div>

				<p>
					<button onClick={this.increment}>Click Me</button>
					{' '}
					Clicked {count} times.
				</p>
			</div>
		);
	}
}
