import plotly.graph_objects as go
import plotly.express as px
import pandas as pd

# Create the figure
fig = go.Figure()

# Define colors for different component types
colors = {
    'Server': '#1FB8CD',      # Strong cyan
    'Client': '#DB4545',      # Bright red  
    'Game Utility': '#2E8B57' # Sea green
}

# Controller Server (top level) - larger and more prominent
fig.add_trace(go.Scatter(
    x=[5], y=[9],
    mode='markers+text',
    marker=dict(size=120, color=colors['Server'], line=dict(width=3, color='white')),
    text=['Controller Server<br>(Python Flask)'],
    textposition='middle center',
    textfont=dict(size=12, color='white', family='Arial Black'),
    name='Server',
    hovertemplate='<b>Controller Server (Python Flask)</b><br>Manages all client commands<br>REST API endpoints<extra></extra>'
))

# Microbot Clients (middle level) - more compact spacing
client_positions = [2.5, 4.5, 6.5, 7.5]
client_names = ['Client 1', 'Client 2', 'Client 3', '100+ Clients']
for i, x in enumerate(client_positions):
    fig.add_trace(go.Scatter(
        x=[x], y=[6],
        mode='markers+text',
        marker=dict(size=80, color=colors['Client'], line=dict(width=2, color='white')),
        text=[f'{client_names[i]}<br>CommandBridge<br>Plugin'],
        textposition='middle center',
        textfont=dict(size=10, color='white'),
        name='Client' if i == 0 else None,
        showlegend=True if i == 0 else False,
        hovertemplate=f'<b>{client_names[i]}</b><br>CommandBridge Plugin included<br>HTTP REST API connection<extra></extra>'
    ))

# Game Utilities (bottom level) - more compact
utilities = ['Rs2Walker', 'Rs2Bank', 'Rs2Inventory', 'Rs2Equipment', 'Rs2Player']
util_positions = [1.5, 3, 4.5, 6, 7.5]
for i, (utility, x) in enumerate(zip(utilities, util_positions)):
    fig.add_trace(go.Scatter(
        x=[x], y=[3],
        mode='markers+text',
        marker=dict(size=65, color=colors['Game Utility'], line=dict(width=2, color='white')),
        text=[utility],
        textposition='middle center',
        textfont=dict(size=9, color='white'),
        name='Game Utility' if i == 0 else None,
        showlegend=True if i == 0 else False,
        hovertemplate=f'<b>{utility}</b><br>Executes game actions<extra></extra>'
    ))

# Add bidirectional arrows between server and clients with API endpoint labels
api_endpoints = ['/api/commands/send', '/api/commands/broadcast', '/api/status', '/api/clients']
for i, x in enumerate(client_positions):
    # Downward arrow (Server to Client)
    fig.add_annotation(
        x=x, y=6.6,
        ax=5, ay=8.4,
        arrowhead=3,
        arrowsize=1.5,
        arrowwidth=3,
        arrowcolor='#5D878F',
        showarrow=True
    )
    # Upward arrow (Client to Server)
    fig.add_annotation(
        x=5.2, y=8.4,
        ax=x+0.2, ay=6.6,
        arrowhead=3,
        arrowsize=1.5,
        arrowwidth=3,
        arrowcolor='#5D878F',
        showarrow=True
    )
    
    # Add API endpoint labels
    if i < len(api_endpoints):
        fig.add_annotation(
            x=(x + 5) / 2 - 0.5, y=7.5,
            text=api_endpoints[i],
            showarrow=False,
            font=dict(size=8, color='#5D878F'),
            bgcolor='rgba(255,255,255,0.8)',
            bordercolor='#5D878F',
            borderwidth=1
        )

# Add arrows from clients to utilities showing command flow
command_flow_pairs = [(2.5, 1.5), (4.5, 3), (6.5, 4.5), (7.5, 6)]
commands = ['walk_to', 'bank_ops', 'inventory', 'equipment']
for (client_x, util_x), cmd in zip(command_flow_pairs, commands):
    fig.add_annotation(
        x=util_x, y=3.7,
        ax=client_x, ay=5.3,
        arrowhead=3,
        arrowsize=1.5,
        arrowwidth=3,
        arrowcolor='#2E8B57',
        showarrow=True
    )
    # Add command labels
    fig.add_annotation(
        x=(client_x + util_x) / 2 + 0.3, y=4.5,
        text=cmd,
        showarrow=False,
        font=dict(size=8, color='#2E8B57'),
        bgcolor='rgba(255,255,255,0.8)',
        bordercolor='#2E8B57',
        borderwidth=1
    )

# Add arrows from utilities to game actions
for x in util_positions:
    fig.add_annotation(
        x=x, y=2.3,
        ax=x, ay=2.7,
        arrowhead=3,
        arrowsize=1.5,
        arrowwidth=3,
        arrowcolor='#D2BA4C',
        showarrow=True
    )

# Add system feature labels
fig.add_annotation(
    x=1, y=8.5,
    text="HTTP REST API<br>(Simple & Scalable)",
    showarrow=False,
    font=dict(size=11, color='black'),
    bgcolor='rgba(255,255,255,0.9)',
    bordercolor='#1FB8CD',
    borderwidth=2
)

fig.add_annotation(
    x=8.5, y=5.5,
    text="100+ Clients<br>Supported",
    showarrow=False,
    font=dict(size=11, color='black'),
    bgcolor='rgba(255,255,255,0.9)',
    bordercolor='#DB4545',
    borderwidth=2
)

fig.add_annotation(
    x=5, y=1.5,
    text="Game Actions Executed",
    showarrow=False,
    font=dict(size=11, color='black'),
    bgcolor='rgba(255,255,255,0.9)',
    bordercolor='#2E8B57',
    borderwidth=2
)

# Update layout
fig.update_layout(
    title='CommandBridge System Architecture',
    xaxis=dict(
        showgrid=False,
        showticklabels=False,
        zeroline=False,
        range=[0.5, 9.5]
    ),
    yaxis=dict(
        showgrid=False, 
        showticklabels=False,
        zeroline=False,
        range=[0.5, 10.5]
    ),
    legend=dict(
        orientation='h',
        yanchor='bottom',
        y=1.02,
        xanchor='center', 
        x=0.5
    ),
    plot_bgcolor='white',
    paper_bgcolor='white',
    showlegend=True
)

# Save the chart
fig.write_image('commandbridge_architecture.png')