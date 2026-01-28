#
# This is the source code of Starwise for Android v. 10.x.x.
# It is licensed under GNU GPL v. 2 or later.
# You should have received a copy of the license in this archive (see LICENSE).
#
# Copyright Gleb Obitocjkiy, 2026.
#

from gigachat import GigaChat
from gigachat.models import Chat, Messages, MessagesRole

def generate_roadmap(prompt , KeyToUse):
    with GigaChat(
            credentials=KeyToUse,
            verify_ssl_certs=False,
            scope="GIGACHAT_API_PERS",
    ) as client:
        response = client.chat(
            Chat(
                messages=[Messages(role=MessagesRole.USER, content=prompt)],
                model="GigaChat",
            )
        )
        return response.choices[0].message.content
