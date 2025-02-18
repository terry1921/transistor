/*
 * NotificationHelper.kt
 * Implements the NotificationHelper class
 * A NotificationHelper creates and configures a notification
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-22 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import org.y20k.transistor.Keys
import org.y20k.transistor.R


@UnstableApi /*
 * NotificationHelper class
 */
class NotificationHelper(private val context: Context) {

    /* Define log tag */
    private val TAG: String = NotificationHelper::class.java.simpleName


    /* Main class variables */
    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    /* Builds a notification for given media session and controller */
    fun getNotification(session: MediaSession, actionFactory: MediaNotification.ActionFactory): Notification {
        ensureNotificationChannel()
        val player: Player = session.player
        val metadata = player.mediaMetadata
        val builder = NotificationCompat.Builder(context, Keys.NOW_PLAYING_NOTIFICATION_CHANNEL_ID)

        // Skip to previous action - duration is set via setSeekBackIncrementMs
        builder.addAction(
            actionFactory.createMediaAction(
                session,
                IconCompat.createWithResource(context, R.drawable.ic_notification_skip_to_previous_36dp),
                context.getString(R.string.notification_skip_to_previous),
                Player.COMMAND_SEEK_TO_PREVIOUS
            )
        )
        if (player.playbackState == Player.STATE_ENDED || !player.playWhenReady) {
            // Play action.
            builder.addAction(
                actionFactory.createMediaAction(
                    session,
                    IconCompat.createWithResource(context, R.drawable.ic_notification_play_36dp),
                    context.getString(R.string.notification_play),
                    Player.COMMAND_PLAY_PAUSE
                )
            )
        } else {
            // Pause action.
            builder.addAction(
                actionFactory.createMediaAction(
                    session,
                    IconCompat.createWithResource(context, R.drawable.ic_notification_stop_36dp),
                    context.getString(R.string.notification_stop),
                    Player.COMMAND_PLAY_PAUSE
                )
            )
        }
        // Skip to next action - duration is set via setSeekForwardIncrementMs
        builder.addAction(
            actionFactory.createMediaAction(
                session,
                IconCompat.createWithResource(
                    context,
                    R.drawable.ic_notification_skip_to_next_36dp
                ),
                context.getString(R.string.notification_skip_to_next),
                Player.COMMAND_SEEK_TO_NEXT
            )
        )

        // define media style properties for notification
        val mediaStyle: MediaStyleNotificationHelper.MediaStyle = MediaStyleNotificationHelper.MediaStyle(session)
//            .setShowCancelButton(true) // only necessary for pre-Lollipop (SDK < 21)
//            .setCancelButtonIntent(actionFactory.createMediaActionPendingIntent(session, Player.COMMAND_STOP)) // only necessary for pre-Lollipop (SDK < 21)
            .setShowActionsInCompactView(1 /* Show play/pause button only in compact view */)

        // configure notification content
        builder.apply {
            setContentTitle(metadata.title)
            setContentText(metadata.artist)
            setContentIntent(session.sessionActivity)
            setDeleteIntent(actionFactory.createMediaActionPendingIntent(session, Player.COMMAND_STOP.toLong()))
            setOnlyAlertOnce(true)
            setSmallIcon(R.drawable.ic_notification_app_icon_white_24dp)
            setLargeIcon(ImageHelper.getStationImage(context, metadata.artworkUri.toString()))
            setStyle(mediaStyle)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setOngoing(false)
        }

        return builder.build()
    }




    /* Creates a notification channel if necessary */
    private fun ensureNotificationChannel() {
        if (Util.SDK_INT < 26 || notificationManager.getNotificationChannel(Keys.NOW_PLAYING_NOTIFICATION_CHANNEL_ID) != null) return
        val channel = NotificationChannel(Keys.NOW_PLAYING_NOTIFICATION_CHANNEL_ID, context.getString(R.string.notification_now_playing_channel_name), NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

}
