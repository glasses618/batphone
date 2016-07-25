package org.servalproject.rhizome;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.messages.MessagesListActivity;
import org.servalproject.messages.ShowConversationActivity;
import org.servalproject.servald.ServalD;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.servaldna.meshms.MeshMSConversation;
import org.servalproject.servaldna.meshms.MeshMSConversationList;
import org.servalproject.group.GroupService;

public class MeshMS {
	private final ServalBatPhoneApplication app;
	private final SubscriberId sid;
	private static final String TAG="MeshMS";
	public static final String NEW_MESSAGES="org.servalproject.meshms.NEW";

	public MeshMS(ServalBatPhoneApplication app, SubscriberId sid){
		this.app=app;
		this.sid=sid;
	}

	public void bundleArrived(RhizomeManifest_MeshMS meshms) throws RhizomeManifest.MissingField {
    Log.d("MeshMS_","new message!!");

    Log.d("MeshMS_","mysid:" + sid.toString()+" receiver:" + meshms.getRecipient().toString() + " sender:" + meshms.getSender().toString() );
		if (sid.equals(meshms.getRecipient())){
      Log.d("MeshMS_","sender: " + meshms.getSender().toString());
			initialiseNotification();
      Intent intent = new Intent(NEW_MESSAGES);
      intent.putExtra("sender", meshms.getSender().toString());
			app.sendBroadcast(intent);
      
      Intent intentGroupService = new Intent(ServalBatPhoneApplication.context, GroupService.class);
      intentGroupService.putExtra("sender", meshms.getSender().toString());
      app.startService(intentGroupService);
		}
	}

	public void markRead(SubscriberId recipient){
		try {
			app.server.getRestfulClient().meshmsMarkAllMessagesRead(sid, recipient);
		} catch (Exception e) {
			app.displayToastMessage(e.getMessage());
			Log.e(TAG, e.getMessage(), e);
		}
		cancelNotification();
		initialiseNotification();
	}

	private int lastMessageHash =0;

	// build an initial notification on startup
	public void initialiseNotification() {
		if (app.isMainThread()){
			app.runOnBackgroundThread(new Runnable() {
				@Override
				public void run() {
					initialiseNotification();
				}
			});
			return;
		}

		if (!ServalD.isRhizomeEnabled())
			return;

		SubscriberId recipient=null;
		boolean unread=false;
		int messageHash=0;
		try {
			MeshMSConversationList conversations = app.server.getRestfulClient().meshmsListConversations(sid);
			MeshMSConversation conv;
			while ((conv = conversations.nextConversation()) != null) {
				// detect when the number of incoming messages has changed
				if (conv.isRead)
					continue;

				messageHash =
						conv.theirSid.hashCode() ^
								(int) conv.lastMessageOffset ^
								(int) (conv.lastMessageOffset >> 32);

				Log.v(TAG, conv.theirSid.abbreviation()+", lastOffset = "+conv.lastMessageOffset+", hash = "+messageHash+", read = "+conv.isRead);

				// remember the recipient, if it is the only recipient with unread messages
				if (unread) {
					recipient = null;
				} else {
					recipient = conv.theirSid;
				}
				unread = true;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		Log.v(TAG, "unread = "+unread+", hash = "+messageHash+", lastHash = "+lastMessageHash);
		if (!unread){
			cancelNotification();
			return;
		}
		if (messageHash == lastMessageHash)
			return;
		lastMessageHash = messageHash;
		// For now, just indicate that there are some unread messages
		String content = "Unread message(s)";
		Intent intent = new Intent(Intent.ACTION_MAIN);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (recipient==null) {
			intent.setClass(app, MessagesListActivity.class);
		}else{
			intent.setClass(app, ShowConversationActivity.class);
			intent.putExtra("recipient", recipient.toHex().toUpperCase());
		}

		PendingIntent pendingIntent = PendingIntent.getActivity(app, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification n = new Notification(R.drawable.ic_serval_logo,
				content,
				System.currentTimeMillis());

		n.setLatestEventInfo(app,
				"", content, pendingIntent);
		n.defaults |= Notification.DEFAULT_VIBRATE
				| Notification.DEFAULT_LIGHTS;

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(app);
		String sound = settings.getString("meshms_notification_sound",
				null);
		if (sound==null)
		if (sound == null) {
			n.defaults |= Notification.DEFAULT_SOUND;
		} else {
			n.sound = Uri.parse(sound);
		}

		n.flags |= Notification.FLAG_SHOW_LIGHTS
				| Notification.FLAG_AUTO_CANCEL;

		NotificationManager nm = (NotificationManager) app
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify("meshms", ServalBatPhoneApplication.NOTIFY_MESSAGES, n);
	}

	public void cancelNotification() {
		NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel("meshms", ServalBatPhoneApplication.NOTIFY_MESSAGES);
	}
}
