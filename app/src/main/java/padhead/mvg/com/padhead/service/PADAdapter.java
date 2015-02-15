package padhead.mvg.com.padhead.service;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import padhead.mvg.com.padhead.R;
import padhead.mvg.com.padhead.solver.Match;
import padhead.mvg.com.padhead.solver.PADSolution;

/**
 * Adapter to populate the solutions browser ListView
 * Author: Maxim Gomov
 */
public class PADAdapter extends BaseAdapter{
	ArrayList<PADSolution> solutions;
	protected LayoutInflater layoutInflater;
	protected PADHeadOverlayService serviceAccessor;
	public PADAdapter(Context ctx, ArrayList<PADSolution> solutions, PADHeadOverlayService svc){
		this.solutions = solutions;
		serviceAccessor = svc;
		layoutInflater = LayoutInflater.from(ctx);
	}

	@Override
	public int getCount() {
		return solutions.size();
	}

	@Override
	public Object getItem(int position) {
		return solutions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// this adapter just formats a String for the ListView... shows what orb combos are present
		// in the represented solution, how many of each combo, what the weight was, how many combos,
		// how long the path is

		convertView = layoutInflater.inflate(R.layout.solution_lvrow, parent, false);
		TextView wv = (TextView)convertView.findViewById(R.id.slr_weight);
		TextView mt = (TextView)convertView.findViewById(R.id.slr_matches);

		PADSolution solution = solutions.get(position);
		DecimalFormat df = new DecimalFormat(".##");

		String text = "";

		String red, blue, green, light, purple, heart, junk;
		red = "";
		blue = "";
		green = "";
		light = "";
		purple = "";
		heart = "";
		junk = "";

		boolean cr, cb, cg, cy, cp, ch, cj;
		cr = cb = cg = cy = cp = ch = cj = false;
		for(Match m : solution.getMatches()){
			switch(m.getType()){
				case 'r':
					red += m.getCount() + "r ";
					cr = true;
					break;
				case 'b':
					blue += m.getCount() + "b ";
					cb = true;
					break;
				case 'g':
					green += m.getCount() + "g ";
					cg = true;
					break;
				case 'y':
					light += m.getCount() + "y ";
					cy = true;
					break;
				case 'p':
					purple += m.getCount() + "p ";
					cp = true;
					break;
				case 'h':
					heart += m.getCount() + "h ";
					ch = true;
					break;
				case 'u':
					junk += m.getCount() + "U ";
					cj = true;
					break;
			}
		}
		text += "W: " + df.format(solution.getWeight()) +  "\t\t\t#C: " + solution.getMatches().size() + "\t\t\tL: " + (solution.getPath().size() + 1) + "\n" ;
		if(cr){
			text += "[R]\t";
		} else {
			text += "\t";
		}
		if(cb){
			text += "[B]\t";
		} else {
			text += "\t";
		}
		if(cg){
			text += "[G]\t";
		} else {
			text += "\t";
		}
		if(cy){
			text += "[Y]\t";
		} else {
			text += "\t";
		}
		if(cp){
			text += "[P]\t";
		} else {
			text += "\t";
		}
		if(ch){
			text += "[H]\t";
		} else {
			text += "\t";
		}
		if(cj){
			text += "[J]\t";
		} else {
			text += "\t";
		}


		text += "\n" + red + '\n' + blue + '\n' + green + '\n' + light + '\n' + purple + '\n' + heart + junk;

		((TextView)convertView.findViewById(R.id.lvrow_mainText)).setText(text);
		((TextView)convertView.findViewById(R.id.lvrow_position)).setText("" + position);

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				serviceAccessor.setActiveSolution(solutions.get(Integer.parseInt(((TextView)v.findViewById(R.id.lvrow_position)).getText().toString())));
				String str = "";
				for(Match m : solutions.get(Integer.parseInt(((TextView)v.findViewById(R.id.lvrow_position)).getText().toString())).getMatches()){
					str += m.getType() + "" + m.getCount() + " ";
				}
				((TextView)(v.getRootView().findViewById(R.id.tv_currentSolution))).setText("Selected Combo:\n" + str);
			}
		});
		return convertView;
	}
}
