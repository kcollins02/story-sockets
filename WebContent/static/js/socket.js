(function ($) {
	var websocket;
	var currentUser;
	var userIsSM;
	
	var elements = {
		login : $('.login'),
		contentArea : $('.main-content > .content'),
		statusArea : $('.main-content .status .cards'),
		cardTemplate : $('.templates > .card').clone(),
		loginError : $('.login .error'),
		controls : $('.vote-control')
	};
	
	var ui = {
		show : function (element) {
			element.removeClass('hidden');
		},
		hide : function (element) {
			element.addClass('hidden');
		},
		setRole : function (issm) {
			elements.contentArea.addClass(issm === true ? "scrum-master" : "team-member");
		},
		updateCards : function (users) {
			for(var i in users) {
				ui.updateCard(users[i]);
			}
		},
		updateCard : function (user) {
			var card = ui.getUserCard(user.username);
			
			if(user.isScrumMaster === true) {
				user.vote = "Scrum Master";
			}
			
			if(card.length === 0) {
				card = elements.cardTemplate.clone();

				card.children('.name').text(user.username);
				card.attr('data-username', user.username);
				
				elements.statusArea.append(card);
			}
			
			card.children('.value').text(user.vote);
		},
		removeCard : function (username) {
			var card = ui.getUserCard(username);
			card.remove();
		},
		userVoted : function (username) {
			var card = ui.getUserCard(username);
			card.addClass('voted');
		},
		clearVoted : function () {
			elements.statusArea.children().removeClass('voted');
		},
		getUserCard : function (username) {
			return elements.statusArea.children('[data-username="' + username + '"]');
		},
		buildControls : function () {
			var controls;
			var card;
			
			if(userIsSM === true) {
				controls = ['Start Round', 'End Round'];
			} else {
				controls = ['0','1','2','3','5','8','13','21','34','55','89','144','&#8734;'];
			}
			
			for(var i in controls) {
				card = elements.cardTemplate.clone();
				
				card.children('.value').html(controls[i]);
				card.attr('data-type', controls[i]);
				
				elements.controls.append(card);
				
				card.on('click', function () {
					var element = $(this)
					var value = $('<textarea />').html(element.data('type')).text();
					socket.send(value);
				});
			}
		},
		setGameState : function (gameState) {
			elements.controls.removeClass('state-start');
			elements.controls.removeClass('state-end');
			elements.controls.addClass('state-' + gameState);
		}
	};
	
	var socket = {
		onmessage : function (event) {
	        console.log(event.data);
	        messageHandler.message(event.data);
		},
		onclose : function (event) {
			console.log(event);
			if(event.code === 1008) {
				messageHandler.close(event);
			}
		},
		connect : function (username, issm) {
			currentUser = username;
			userIsSM = issm;
			
			ui.setRole(issm);
			
		    var host = document.location.host;
		    var pathParts = document.location.pathname.split('/');
		    pathParts.pop();
		    var pathname = pathParts.join('/');
		    
			websocket = new WebSocket("ws://" +host  + pathname + "/vote/" + username + "/" + issm);
			
			websocket.onmessage = socket.onmessage;
			websocket.onclose = socket.onclose;
		},
		close : function () {
			websocket.onclose = function () {};
		    websocket.close();
		    console.log('sent')
		},
		send : function (content) {
		    websocket.send(
	    		JSON.stringify({
			        "content":content
			    })
		    );
		}
	};
	
	var messageHandler = {
		connect : function (data) {
			ui.hide(elements.login);
			ui.show(elements.contentArea);
			
			ui.updateCards(data.users);
			
			if(data.from === currentUser) {
				ui.buildControls();
			}
			
			if(data.gameState !== undefined) {
				ui.setGameState(data.gameState);
			}
		},
		message : function (data) {
			var message = JSON.parse(data);
	        
        	switch (message.content) {
	        	case 'Start Round':
	        		ui.clearVoted();
	        		break;
	        	case 'End Round':
	        		break;
	        	default:
	        		try {
	    	        	messageHandler[message.content](message);
	    	        } catch (e) {
	    	        	console.log('Message not found:', message.content);
	    	        }
	        		break;
        	}

			ui.updateCards(message.users);
			
			if(message.gameState !== undefined) {
				ui.setGameState(message.gameState);
			}
		},
		disconnect : function (data) {
			ui.removeCard(data.from);
		},
		waiting : function (data) {},
		allin : function (data) {},
		vote : function (data) {
			ui.userVoted(data.from);
		},
		results : function (data) {},
		close : function (data) {
			currentUser = null;
			userIsSM = null;
			ui.hide(elements.contentArea);
			ui.show(elements.login);
			elements.loginError.text(data.reason);
		}
	};
	
	var events = {
		login : function (e) {
			var username = $('input.username').val();
			var issm = $('input.scrum-master').is(':checked');
			e.preventDefault();
			socket.connect(username, issm);
		},
		logout : function (e) {
			socket.close();
		}
	};
	
	$('.login form').on('submit', events.login);
	$(window).on('beforeunload', events.logout);
	
})(jQuery);