
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
const moment = require('moment');
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/*
exports.tokenUpdatedAt = functions.database.ref("USERS/{userId}/token")
.onWrite(event => {	
	const userRef = event.data.ref.parent;
	const tokenUpdatedAt = userRef.child('tokenUpdatedAt');
	const userId = event.params.userId;

	date = new Date();
	var formatDate =  date.getDate()+"-"+(date.getMonth()+1)+" "+date.getHours()+":"+date.getMinutes();
	return tokenUpdatedAt.set(formatDate).then(() => {
		return console.log('Token just updated by user: ', userId );	
	});
});
*/

exports.commentsWatcher = functions.database.ref("/DIALOGS_MESSAGES/{dialogId}/{commentId}")
.onCreate((event, context) => {
	const commentObj = event.val();
	const root = event.ref.root;	
	const dialogId = context.params.dialogId;
	const userAvaRef = event.ref.child('userAvatar');
	const dialogPromise = root.child(`/DIALOGS/${dialogId}`).once('value'); 
	const usersPromise = root.child(`USERS`).once('value'); 
	return Promise.all([dialogPromise, usersPromise]).then((results)=>{		
		const dialogSnap = results[0];
		const snapshotU = results[1];
		var dialogObj = dialogSnap.val();
		// update last Comment// update updaetedAt
		dialogSnap.ref.child('lastComment').set(commentObj.userName + ": " + commentObj.message);		
		dialogSnap.ref.child('updatedAt').set(commentObj.date);				
		var tokens = [];			
		snapshotU.forEach(userSnap => {	
			const userObj = userSnap.val();				
			if(userObj.id !== commentObj.userId && userObj.isReceiveNotifications) {
				if (userObj.isAdmin) {
					console.log("Update notifications for ", userObj.fullName);						
					root.child(`/USERS_NOTIFICATIONS`).child(`/${userObj.id}/${dialogId}/key`).set(`${dialogId}`);
					if(userObj.token){
						console.info("Token Added for User: " + userObj.fullName +", Token: ", userObj.token);
						tokens.push(userObj.token);		
					}			
				} else {
					dialogSnap.child('Members').forEach(member =>{
						if(userObj.id === member.key){				
							// update Notifications
							console.log("Update notifications for ", userObj.fullName);						
							root.child(`/USERS_NOTIFICATIONS`).child(`/${userObj.id}/${dialogId}/key`).set(`${dialogId}`);
							if(userObj.token ){
								console.info("Token Added for User: " + userObj.fullName +", Token: ", userObj.token);
								tokens.push(userObj.token);		
							}							
						} 	
					})
				}
			}	
		});					
		var payload = {
			data:{						
				title: dialogObj.title1,
				message: commentObj.userName + ": " + commentObj.message,			
				messageKey: dialogId,
				senderId: commentObj.userId,
				senderName: commentObj.userName,				
				type: "COMMENT"
			}
		};
		return sendNessagesViaFCM(tokens, payload);					
	});
});

exports.expenseWatcher = functions.database.ref("/EXPENSES/{year}/{month}/{reportId}")
.onWrite((event, context) => {
    var expenseObj = event.after.val();
    const month = context.params.month;
    const year = context.params.year;
	var aTitle = "Витрату оновлено";
	var aMessage = ": ";	

	if(!event.after.exists() && event.before.exists()){
		console.log("Expense removed ");
		expenseObj = event.before.val();
		aTitle = "Витрату видалено";
		aMessage = ": ";
	} else
	if(event.after.exists() && !event.before.exists()){
		console.log("Expense created ");		
		aTitle = "Витрату створено";
		aMessage = ": ";
	} else{
		console.log("Expense updated");		
    }
    const date = expenseObj.expenseDate.date+"."+ month+"."+year;
	var payload = {
		data:{						
			title: aTitle,
			message: expenseObj.userName + aMessage + expenseObj.title1+" ("+ expenseObj.price+" грн)",				
			expenseUserName: expenseObj.userName,
			expenseUserId: expenseObj.userId,
			expenseDate: date,
			type: "EXPENSE"
		}
	};
	return sendMessagesToAdminsExceptMe(payload, expenseObj.userId);		
});

exports.lessonWatcher = functions.database.ref("/LESSONS/{year}/{month}/{day}/{reportId}")
.onWrite((event, context) => {
    var lessonObj = event.after.val();
    const month = context.params.month;
	const year = context.params.year;
	const day = context.params.day;
	var aTitle = "Заняття оновлено";	

	if(!event.after.exists() && event.before.exists()){
		console.log("Lesson removed ");
		lessonObj = event.before.val();
		aTitle = "Заняття видалено";		
	} else
	if(event.after.exists() && !event.before.exists()){
		console.log("Lesson created ");		
		aTitle = "Заняття створено";		
	} else{
		console.log("Lesson updated");		
	}

	var minutes=lessonObj.dateTime.minutes;
	if(lessonObj.dateTime.minutes<10)
		minutes = "0" + minutes;
	if(lessonObj.dateTime.minutes===0)
		minutes = "00";
		
    const date = day+"."+ month+"."+year;
	var payload = {
		data:{						
			title: aTitle,
			message: lessonObj.userName + " " + date + " " + lessonObj.dateTime.hours + ":" + minutes,
			lessonUserName: lessonObj.userName,
			lessonUserId: lessonObj.userId,
			lessonKey: lessonObj.key,			
			lessonDate: date,
			type: "LESSON"
		}
	};
	return sendMessagesToAdminsExceptMe(payload, lessonObj.userId);		
});


exports.contactWatcher = functions.database.ref("/CONTACTS/{contactId}")
.onWrite((event, context) => {
    var contactObj = event.after.val();
	var aTitle = "Контакт оновлено";	

	if(!event.after.exists() && event.before.exists()){
		console.log("Contact removed ");
		contactObj = event.before.val();
		aTitle = "Контакт видалено";		
	} else
	if(event.after.exists() && !event.before.exists()){
		console.log("Contact created ");		
		aTitle = "Контакт створено";		
	} else{
		console.log("Contact updated");		
	}
			
	var aMessage = contactObj.name + "  " + contactObj.phone;
	if(contactObj.userName){
		aMessage = aMessage + " [" + contactObj.userName + "]";
	}
	var payload = {
		data:{						
			title: aTitle,
			message: aMessage,
			contactKey: contactObj.key,
			type: "CONTACT"
		}
	};
	return sendMessagesToAdmins(payload);		
});


function sendMessagesToAdmins(payload){
	return sendMessagesToAdminsExceptMe(payload, "");
}

function sendMessagesToAdminsExceptMe(payload, uId){
	var tokens = [];
	return admin.database().ref(`/USERS`).once('value').then(snapshot => {
		snapshot.forEach(user => {	
			userObj = user.val();
			if(userObj.isAdmin && userObj.token && userObj.id !== uId){	
				if(userObj.isReceiveNotifications){
					console.info("Token Added for: " + userObj.fullName +", userId: ", userObj.id);									
					tokens.push(userObj.token);
				} else {
					console.info("Token Skipped for: " + userObj.fullName +", userId: ", userObj.id);									
				}
			}			
		});//end for

		return sendNessagesViaFCM(tokens, payload);
	});
}


function sendMessagesToUsers(payload, uIds){
	var tokens = [];
	return admin.database().ref(`/USERS`).once('value').then(snapshot => {
		snapshot.forEach(user => {	
			userObj = user.val();
			if(userObj.token  && uIds.includes(userObj.id)){	
				if(userObj.isReceiveNotifications){
					console.info("Token Added for: " + userObj.fullName +", userId: ", userObj.id);									
					tokens.push(userObj.token);
				} else {
					console.info("Token Skipped for: " + userObj.fullName +", userId: ", userObj.id);									
				}
			}			
		});//end for

		return sendNessagesViaFCM(tokens, payload);
	});
}

function sendNessagesViaFCM(tokens, payload){
	if(tokens.length > 0)
		return  admin.messaging().sendToDevice(tokens, payload)
			.then(response => {
				console.log("Push Sent: ", response);
				return 0;
			})
			.catch(error => {
				console.log("Push Error: ", error);
			});			
	else 
		return console.log("Push No Tokens");
}


// HTTP


exports.before10 = functions.https.onRequest((req, res)=>{	
	const key = req.query.key;
	//firebase functions:config:set cron.key="somecoolkey"
	// Exit if the keys don't match.
	if (key !== functions.config().cron.key) {
		console.log('The key provided in the request does not match the key set in the environment. Check that', key,
			'matches the cron.key attribute in `firebase env:get`');
		res.status(403).send('Security key does not match. Make sure your "key" URL query parameter matches the ' +
			'cron.key environment variable.');
		return null;
	}
	const date = moment().format("YYYY/MM/DD");	
	var hrsUTC = parseInt(moment().format("H"));	
	var mnt = parseInt(moment().format("m"));	
	const refString = '/LESSONS/' + date;
	console.log(refString + " UTC "+ hrsUTC + ":" + mnt);
	const contactsPromise = admin.database().ref("/CONTACTS").once('value');	
	const lessonsPromise = admin.database().ref(refString).once('value');	
	return Promise.all([contactsPromise, lessonsPromise]).then((result)=> {		
		const contactsSnap =result[0];
		const lessonsSnap =result[1];
			var count = 0;			
			var messages = 0;			
			lessonsSnap.forEach(lesson => {	
				var lessonObj = lesson.val();				
				count = count + 1;		
				var offset = lessonObj.dateTime.timezoneOffset / 60;	
				var hrs = hrsUTC - offset;
				if((lessonObj.dateTime.hours === hrs && lessonObj.dateTime.minutes > mnt && lessonObj.dateTime.minutes < (mnt+11))
					|| (lessonObj.dateTime.hours === (hrs+1) && mnt > 49 && lessonObj.dateTime.minutes === 0)) {					
						const membersRef = admin.database().ref(refString+"/"+lessonObj.key+"/clients/");
						sendLessonRemainder(lessonObj.userId, contactsSnap, membersRef);
						sendReminderIfFirstLesson(lessonObj, lessonsSnap);
						messages = messages + 1;
				}
			});	
			const logStr = "Today found " + count + " lessons. Messages sent to " + messages + " users"; 
			console.log(logStr);
			res.status(200).send(logStr);
			return 0;		
	});
});

function sendReminderIfFirstLesson(lessonObj, lessonsSnap){
	const reminderPromise = admin.database().ref("/REMINDERS").once('value');	
	return reminderPromise.then(reminderSnap => {
		const reminderObj = reminderSnap.val();			
		var beforeCount = 0;
		//var afterCount = 0;
		lessonsSnap.forEach(lesson => {	
			var lessonO = lesson.val();				
			if(lessonO.dateTime.time < lessonObj.dateTime.time){
				beforeCount = beforeCount + 1;
			}
		//	if(lessonO.dateTime.time > lessonObj.dateTime.time){
		//		afterCount = afterCount + 1;
		//	}
		});
		if(beforeCount===0 && reminderObj.beforeLessonReminder && reminderObj.beforeLessonReminder !== ""){
			console.log("Reminder " + lessonObj.userId);
			var usersIds = [];
			usersIds.push(lessonObj.userId);
			var payload = {
				data:{
					title: "Нагадування",
					message:  reminderObj.beforeLessonReminder,
					type: "REMINDER"
				}
			};
			return sendMessagesToUsers(payload, usersIds);
		} else {
			return 0;	
		}
	});
}

function sendLessonRemainder(userId, contactsSnap, membersRef) {	
	return membersRef.once('value').then(membersSnap => {
		var membersString="";		
		var usersIds = [];		
		contactsSnap.forEach(contact=>{
			var contactObj = contact.val();
			membersSnap.forEach(member=>{
				var membrObj = member.val();
				if(contactObj.key === membrObj) {
					if(membersString!==""){
						membersString = membersString +", ";
					}
					membersString = membersString + contactObj.name;
					console.log("Contact Name "+ contactObj.name);
				}				
			});
		})							
		usersIds.push(userId);
		var payload1 = {
			data:{
				title: "Нагадування про заняття",
				message: "(" + membersString + ")",
				type: "OTHER"
			}
		};
		return sendMessagesToUsers(payload1, usersIds);							
	});	
}
