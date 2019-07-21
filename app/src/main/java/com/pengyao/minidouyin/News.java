package com.pengyao.minidouyin;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pengyao.minidouyin.tool.CircleImageView;
import com.pengyao.minidouyin.tool.Message;

import java.util.List;

import static com.pengyao.minidouyin.tool.PullParser.getMessage;


/**
 * A simple {@link Fragment} subclass.
 */
public class News extends Fragment {

    public News() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.news_rv);
        try {
            recyclerView.setAdapter(new RecyclerView.Adapter() {
                private List<Message> messages = getMessage((Activity)container.getContext());
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = inflater.inflate(R.layout.news_rv_item, null);
                    RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view){};
                    Log.d("hello", "hello" + messages.size());
                    return holder;
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    CircleImageView avatar = holder.itemView.findViewById(R.id.iv_avatar);
                    ImageView notice = holder.itemView.findViewById(R.id.robot_notice);
                    TextView title = holder.itemView.findViewById(R.id.tv_title);
                    TextView description = holder.itemView.findViewById(R.id.tv_description);
                    TextView time = holder.itemView.findViewById(R.id.tv_time);

                    title.setText(messages.get(position).getTitle());
                    description.setText(messages.get(position).getDescription());
                    time.setText(messages.get(position).getTime());
                    if(messages.get(position).isOfficial()){
                        notice.setVisibility(View.VISIBLE);
                    }
                    int icon_id = 0;
                    switch (messages.get(position).getIcon()){
                        case "TYPE_ROBOT": icon_id = R.drawable.session_robot; break;
                        case "TYPE_GAME": icon_id = R.drawable.icon_micro_game_comment; break;
                        case "TYPE_SYSTEM": icon_id = R.drawable.session_system_notice; break;
                        case "TYPE_STRANGER": icon_id = R.drawable.session_stranger; break;
                        case "TYPE_USER": icon_id = R.drawable.icon_girl; break;
                        default: break;
                    }
                    avatar.setImageResource(icon_id);
                    holder.itemView.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(container.getContext(), ChartRoom.class);
                            intent.putExtra("message", "你好，请问有什么问题？");
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public int getItemCount() {
                    Log.d("hello", "" + messages.size());
                    return messages.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        LinearLayoutManager managerVertical = new LinearLayoutManager(container.getContext());
        managerVertical.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(managerVertical);
        recyclerView.setHasFixedSize(true);
        return view;
    }

}
