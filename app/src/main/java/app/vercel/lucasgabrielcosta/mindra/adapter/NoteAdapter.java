package app.vercel.lucasgabrielcosta.mindra.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import app.vercel.lucasgabrielcosta.mindra.R;
import app.vercel.lucasgabrielcosta.mindra.model.Note;

public class NoteAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Note> noteList;
    private SimpleDateFormat dateFormat;

    public NoteAdapter(Context context, ArrayList<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @Override
    public int getCount() {
        return noteList.size();
    }

    @Override
    public Object getItem(int position) {
        return noteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return noteList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.note_list_item, parent, false);

            holder = new ViewHolder();
            holder.tvNoteTitle = convertView.findViewById(R.id.tvNoteTitle);
            holder.tvCategory = convertView.findViewById(R.id.tvCategory);
            holder.tvNoteContent = convertView.findViewById(R.id.tvNoteContent);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvConnections = convertView.findViewById(R.id.tvConnections);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Note note = noteList.get(position);

        holder.tvNoteTitle.setText(note.getTitle());
        holder.tvCategory.setText(note.getCategory());
        holder.tvNoteContent.setText(note.getContentPreview(100));
        holder.tvDate.setText(dateFormat.format(note.getCreationDate()));

        int connectionCount = note.getConnectionCount();
        holder.tvConnections.setText(connectionCount + " " +
                (connectionCount == 1 ? context.getString(R.string.connection_single) :
                        context.getString(R.string.connection_plural)));

        return convertView;
    }

    private static class ViewHolder {
        TextView tvNoteTitle;
        TextView tvCategory;
        TextView tvNoteContent;
        TextView tvDate;
        TextView tvConnections;
    }
}